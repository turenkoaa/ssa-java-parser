package com.kspt.aaturenko.diagram;

import com.kspt.aaturenko.ssa_tree.SSABlock;
import com.kspt.aaturenko.ssa_tree.SSAExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anastasia on 05.11.2018.
 */
public class VersionResolver {
    private Map<String, Integer> varNumbers = new HashMap<>();

    public SSABlock resolve(SSABlock ssaBlock) {
        SSAExpression ssaBlockExpression = ssaBlock.getExpression();
        String declaredVar = ssaBlockExpression.getLeft().getVarName();

        Integer version = varNumbers.get(declaredVar);
        resolveVersionedVars(ssaBlockExpression);
        varNumbers.put(declaredVar, version != null ? version + 1 : 1);
        ssaBlockExpression.getLeft().setVarName(declaredVar + "_" + varNumbers.get(declaredVar));
        return ssaBlock;
    }

    private void resolveVersionedVars(SSAExpression ssaBlockExpression) {
        String varName = ssaBlockExpression.getVarName();
        String numericValue = ssaBlockExpression.getNumericValue();
        if (varName != null) {
            ssaBlockExpression.setVarName(varName + "_" + varNumbers.get(varName));
        } else if (numericValue == null) {
            resolveVersionedVars(ssaBlockExpression.getLeft());
            resolveVersionedVars(ssaBlockExpression.getRight());
        }
    }

}
