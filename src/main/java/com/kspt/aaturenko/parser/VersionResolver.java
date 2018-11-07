package com.kspt.aaturenko.parser;

import com.kspt.aaturenko.ssa_tree.SSABlock;
import com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType;
import com.kspt.aaturenko.ssa_tree.SSAExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType.*;

/**
 * Created by Anastasia on 05.11.2018.
 */
public class VersionResolver {
    private Map<String, Integer> varNumbers = new HashMap<>();
    List<PhiHelperModel> phiModels = new ArrayList<>();

    public SSABlock resolve(SSABlock ssaBlock) {
        SSAExpression ssaBlockExpression = ssaBlock.getExpression();
        String declaredVar = ssaBlockExpression.getLeft().getVarName();
        Integer version = varNumbers.get(declaredVar);

        resolveVersionedVars(ssaBlockExpression);

        if (ssaBlock.getType() == ASSIGNMENT) {
            varNumbers.put(declaredVar, version != null ? version + 1 : 1);
            ssaBlockExpression.getLeft().setVarName(declaredVar + "_" + varNumbers.get(declaredVar));
        } else if (ssaBlock.getType() == CONDITION) {
//            ssaBlock.getChildren()
            //определи что блок из зен или элс
            //добавь в мапу варВБранче с последней версией
        }
        return ssaBlock;
    }

    private List<PhiHelperModel> countAssignments() {
        List<PhiHelperModel> phi = new ArrayList<>();
        return phi;
    }

    private void resolveVersionedVars(SSAExpression ssaBlockExpression) {
        String varName = ssaBlockExpression.getVarName();
        String numericValue = ssaBlockExpression.getNumericValue();
        if (varName != null) {

            for (PhiHelperModel phiModel : phiModels) {
                Integer versionsInBranch = phiModel.getVarVersionInBranch().get(varName);
                if (versionsInBranch != null) {
                    Integer version = varNumbers.get(varName);
                    int versionOfPhi = version != null ? version + 1 : 1;
                    phiModel.putToPhiFunctions(varName, versionOfPhi);
                    varNumbers.put(varName, versionOfPhi + 1);
                    phiModel.getVarVersionInBranch().remove(varName);
                }
            }

            ssaBlockExpression.setVarName(varName + "_" + varNumbers.get(varName));
        } else if (numericValue == null) {
            resolveVersionedVars(ssaBlockExpression.getLeft());
            resolveVersionedVars(ssaBlockExpression.getRight());
        }
    }

}
