package com.sun.source.tree;

import javax.lang.model.element.Name;
import checkers.inference.reim.quals.*;

public interface BreakTree extends StatementTree {
    @PolyreadThis @Polyread Name getLabel() ;
}
