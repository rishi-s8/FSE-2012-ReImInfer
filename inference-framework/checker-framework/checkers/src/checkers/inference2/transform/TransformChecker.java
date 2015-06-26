/**
 * 
 */
package checkers.inference2.transform;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import checkers.inference2.jcrypt2.quals.RND;
import checkers.inference2.jcrypt2.quals.OPE;
import checkers.inference2.jcrypt2.quals.AH;
import checkers.inference2.jcrypt2.quals.DET;
import checkers.inference2.jcrypt.quals.Clear;
import checkers.inference2.Constraint;
import checkers.inference2.ConstraintSolver.FailureStatus;
import checkers.inference2.InferenceChecker;
import checkers.inference2.Reference;
import checkers.inference2.Reference.AdaptReference;
import checkers.inference2.Reference.FieldAdaptReference;
import checkers.inference2.Reference.MethodAdaptReference;
import checkers.inference2.Reference.RefKind;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceVisitor;
import checkers.util.AnnotationUtils;
import checkers.util.TreeUtils;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.Tag;

/**
 * @author huangw5
 * 
 */
@SupportedOptions({ "warn", "infer", "debug", "noReim", "inferLibrary",
		"polyLibrary", "inferAndroidApp" })
@TypeQualifiers({ RND.class, OPE.class, DET.class, AH.class })
public class TransformChecker extends InferenceChecker {

	public AnnotationMirror RND, OPE, DET, AH, CLEAR;

	private Set<AnnotationMirror> sourceAnnos;

	private List<RefKind> specialRefKinds = null;

	private AnnotationUtils annoFactory;
	
	private CompilationUnitTree root;

	public void initChecker(ProcessingEnvironment processingEnv) {
		super.initChecker(processingEnv);
		annoFactory = AnnotationUtils.getInstance(env);
		RND = annoFactory.fromClass(RND.class);
		OPE = annoFactory.fromClass(OPE.class);
		AH = annoFactory.fromClass(AH.class);
		DET = annoFactory.fromClass(DET.class);
		CLEAR = annoFactory.fromClass(Clear.class);

		sourceAnnos = AnnotationUtils.createAnnotationSet();
		sourceAnnos.add(RND);
		sourceAnnos.add(OPE);
		sourceAnnos.add(AH);
		sourceAnnos.add(DET);

		specialRefKinds = new ArrayList<RefKind>(3);
		specialRefKinds.add(RefKind.STRING);
		specialRefKinds.add(RefKind.NULL);
		specialRefKinds.add(RefKind.EQUAL_NULL);
	}

	public Element getEnclosingMethod(Element elt) {
		while (elt != null && elt.getKind() != ElementKind.METHOD
				&& elt.getKind() != ElementKind.CONSTRUCTOR) {
			elt = elt.getEnclosingElement();
		}
		return elt;
	}

	/**
	 * Check if this constraint connects param/ret
	 * 
	 * @param c
	 * @return
	 */
	public boolean isParamReturnConstraint(Constraint c) {
		Reference left = c.getLeft();
		Reference right = c.getRight();
		if (isParamOrRetRef(left) && isParamOrRetRef(right)) {
			Element lElt = null;
			Element rElt = null;
			// check if they are from the same method
			lElt = getEnclosingMethod(left.getElement());
			rElt = getEnclosingMethod(right.getElement());
			if (lElt.equals(rElt))
				return true;
		}
		return false;
	}

	/**
	 * Check if this ref is param/ret/this
	 * 
	 * @param ref
	 * @return
	 */
	public boolean isParamOrRetRef(Reference ref) {
		if (ref != null && !(ref instanceof AdaptReference)) {
			RefKind kind = ref.getKind();
			return kind == RefKind.PARAMETER || kind == RefKind.THIS
					|| kind == RefKind.RETURN;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#createFieldAdaptReference(checkers
	 * .inference2.Reference, checkers.inference2.Reference,
	 * checkers.inference2.Reference)
	 */
	@Override
	protected Reference createFieldAdaptReference(Reference context,
			Reference decl, Reference assignTo) {
		return new FieldAdaptReference(context, decl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#createMethodAdaptReference(checkers
	 * .inference2.Reference, checkers.inference2.Reference,
	 * checkers.inference2.Reference)
	 */
	@Override
	protected Reference createMethodAdaptReference(Reference context,
			Reference decl, Reference assignTo) {
		// Use callsite as context
		Set<Tree.Kind> kinds = EnumSet.of(Tree.Kind.METHOD_INVOCATION,
				Tree.Kind.NEW_CLASS);
		Tree tree = TreeUtils.enclosingOfKind(currentPath, kinds);
		if (tree != null) {
			// use constant reference
			String identifier = CALLSITE_PREFIX + getIdentifier(tree);
			TypeElement enclosingType = TreeUtils
					.elementFromDeclaration(TreeUtils
							.enclosingClass(currentFactory.getPath(tree)));
			Reference callsiteRef = getAnnotatedReference(identifier,
					RefKind.CALL_SITE, null, null, enclosingType,
					currentFactory.getAnnotatedType(tree));
			return new MethodAdaptReference(callsiteRef, decl);
		} else {
			throw new RuntimeException("Invalid adaptation context!");
		}
	}
	
	// ignore String + String
	protected boolean shouldIgnore(JCBinary t) {
		JCExpression left = t.getLeftOperand();
		JCExpression right = t.getRightOperand();
		boolean ignoreLeft = false, ignoreRight = false;
		if (left instanceof JCBinary) {
			ignoreLeft = shouldIgnore(((JCBinary) left));
		}
		if (right instanceof JCBinary) {
			ignoreRight = shouldIgnore(((JCBinary) right));
		}
		RefKind leftKind = getAnnotatedReference(left).getKind();
		RefKind rightKind = getAnnotatedReference(right).getKind();
		return ignoreLeft || ignoreRight || specialRefKinds.contains(leftKind)
				|| specialRefKinds.contains(rightKind);
	}
	
	// for x ==/!= null, set its RefKind as EQUAL_NULL
	protected void setRefKind(Reference r, JCBinary t) {
		Reference leftRef = getAnnotatedReference(t.getLeftOperand());
		Reference rightRef = getAnnotatedReference(t.getRightOperand());
		if (t.getTag() == Tag.EQ || t.getTag() == Tag.NE) {
			if (leftRef.getKind() == RefKind.NULL
					|| rightRef.getKind() == RefKind.NULL) {
				r.setRefKind(RefKind.EQUAL_NULL);
			}
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#annotateDefault(checkers.inference2
	 * .Reference, checkers.inference2.Reference.RefKind,
	 * javax.lang.model.element.Element, com.sun.source.tree.Tree)
	 */
	@Override
	protected void annotateDefault(Reference r, RefKind kind, Element elt, Tree t) {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#annotateArrayComponent(checkers.
	 * inference2.Reference, javax.lang.model.element.Element)
	 */
	@Override
	protected void annotateArrayComponent(Reference r, Element elt) {
		annotateDefault(r, r.getKind(), elt, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#annotateField(checkers.inference2
	 * .Reference, javax.lang.model.element.Element)
	 */
	@Override
	protected void annotateField(Reference r, Element fieldElt) {
		annotateDefault(r, r.getKind(), fieldElt, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#annotateThis(checkers.inference2
	 * .Reference, javax.lang.model.element.ExecutableElement)
	 */
	@Override
	protected void annotateThis(Reference r, ExecutableElement methodElt) {
		annotateParameter(r, methodElt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#annotateParameter(checkers.inference2
	 * .Reference, javax.lang.model.element.Element)
	 */
	@Override
	protected void annotateParameter(Reference r, Element elt) {
		if (!isAnnotated(r) && !containsAnno(r, CLEAR)) {
			if (r.toString().contains("java.lang.String.equals")) {
				r.addAnnotation(OPE);
				r.addAnnotation(DET);
			} else if (r.toString().equals("java.lang.String.compareTo")) {
				r.addAnnotation(OPE);
			} else {
				annotateDefault(r, r.getKind(), elt, null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#annotateReturn(checkers.inference2
	 * .Reference, javax.lang.model.element.ExecutableElement)
	 */
	@Override
	protected void annotateReturn(Reference r, ExecutableElement methodElt) {
		annotateDefault(r, r.getKind(), methodElt, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#adaptField(javax.lang.model.element
	 * .AnnotationMirror, javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public AnnotationMirror adaptField(AnnotationMirror contextAnno,
			AnnotationMirror declAnno) {
		return adaptMethod(contextAnno, declAnno);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#adaptMethod(javax.lang.model.element
	 * .AnnotationMirror, javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public AnnotationMirror adaptMethod(AnnotationMirror contextAnno,
			AnnotationMirror declAnno) {
		return declAnno;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see checkers.inference2.InferenceChecker#isStrictSubtyping()
	 */
	@Override
	public boolean isStrictSubtyping() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#getFailureStatus(checkers.inference2
	 * .Constraint)
	 */
	@Override
	public FailureStatus getFailureStatus(Constraint c) {
		return FailureStatus.ERROR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see checkers.inference2.InferenceChecker#getSourceLevelQualifiers()
	 */
	@Override
	public Set<AnnotationMirror> getSourceLevelQualifiers() {
		return sourceAnnos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * checkers.inference2.InferenceChecker#getAnnotaionWeight(javax.lang.model
	 * .element.AnnotationMirror)
	 */
	@Override
	public int getAnnotaionWeight(AnnotationMirror anno) {
		if (anno.toString().equals(OPE.toString()))
			return 4;
		else if (anno.toString().equals(DET.toString()))
			return 3;
		else if (anno.toString().equals(AH.toString()))
			return 2;
		else if (anno.toString().equals(RND.toString()))
			return 1;
		else
			return Integer.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see checkers.inference2.InferenceChecker#needCheckConflict()
	 */
	@Override
	public boolean needCheckConflict() {
		return false;
	}

	public boolean containsAnno(Reference ref, AnnotationMirror anno) {
		if (ref instanceof AdaptReference) {
			Reference contextRef = ((AdaptReference) ref).getContextRef();
			Reference declRef = ((AdaptReference) ref).getDeclRef();
			if (ref instanceof FieldAdaptReference) {
				if (containsAnno(contextRef, anno) || containsAnno(declRef, anno)) {
					return true;
				}
			} else if (containsAnno(declRef, anno)) {
				return true;
			}
		}
		for (AnnotationMirror a : ref.getRawAnnotations()) {
			if (a.toString().equals(anno.toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected SourceVisitor<?, ?> getInferenceVisitor(
			InferenceChecker inferenceChecker, CompilationUnitTree root) {
		this.root = root;
		return new TransformVisitor(this, root);
	}

	@Override
	public void printResult(PrintWriter out) {
		out.print(this.root.toString().replace("@Sensitive()", "").replace("@Poly()", ""));
	}

//	@Override
//	public Reference getAnnotatedReference(String identifier, RefKind kind,
//			Tree tree, Element element, TypeElement enclosingType,
//			AnnotatedTypeMirror type, Set<AnnotationMirror> annos) {
//		Reference ret = super.getAnnotatedReference(identifier, kind, tree,
//				element, enclosingType, type, annos);
//		if (!isAnnotated(ret)) {
//			// we have reim and jcrypt annotation, now we want to add encryption annotation
//			Set<AnnotationMirror> oldAnnos = ret.getRawAnnotations();
//			annotatedReferences.remove(identifier);
//			if (containsAnno(ret, CLEAR)) {
//				annos = AnnotationUtils.createAnnotationSet();
//				annos.add(CLEAR);
//			}
//			Reference newRef = super.getAnnotatedReference(identifier, kind,
//					tree, element, enclosingType, type, annos);
//			for (AnnotationMirror anno : oldAnnos) {
//				newRef.addAnnotation(anno);
//			}
//			annotatedReferences.put(identifier, newRef);
//			return newRef;
//		}
//		return ret;
//	}

	@Override
	public Set<AnnotationMirror> adaptFieldSet(
			Set<AnnotationMirror> contextSet, Set<AnnotationMirror> declSet) {
		return declSet;
	}

	@Override
	public Set<AnnotationMirror> adaptMethodSet(
			Set<AnnotationMirror> contextSet, Set<AnnotationMirror> declSet) {
		return declSet;
	}

}
