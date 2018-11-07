package com.kspt.aaturenko.parser;

import com.kspt.aaturenko.ssa_tree.SSABlock;
import com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType;
import com.kspt.aaturenko.ssa_tree.SSAExpression;

import java.util.*;

import static com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType.*;

/**
 * Created by Anastasia on 05.11.2018.
 */
public class VersionResolver {
    private Map<String, Integer> varNumbers = new HashMap<>();
    List<PhiHelperModel> phiModels = new ArrayList<>();

    public void resolveVersions(SSABlock ssaBlock){
        resolve(ssaBlock);
        phiModels.forEach(phi -> System.out.println(phi.getPhiFunctions()));
    }

    public void resolve(SSABlock ssaBlock) {
        SSAExpression ssaBlockExpression = ssaBlock.getExpression();
        String declaredVar = ssaBlockExpression.getLeft().getVarName();
        Integer version = varNumbers.get(declaredVar);

        resolveVersionedVars(ssaBlockExpression);

        if (ssaBlock.getType() == ASSIGNMENT) {
            varNumbers.put(declaredVar, version != null ? version + 1 : 1);
            ssaBlockExpression.getLeft().setVarName(declaredVar + "_" + varNumbers.get(declaredVar));
            ssaBlock.getChildren().forEach(this::resolve);
        }  else if (ssaBlock.getType() == CONDITION) {
            Set<String> varsFromBranches = new HashSet<>();
            ssaBlock.getChildren().forEach(branch -> varsFromBranches.addAll(getVarsFromBranch(branch)));
            ssaBlock.getChildren().forEach(this::resolve);

            // достань конечные версии переменных для каждой ветки отдельно
            // храни их так - мапа: вар -> список версий
            // и дльше выдавай такую же структуру на вывод
            PhiHelperModel phiHelperModel = new PhiHelperModel();
            for (String var : varsFromBranches) {
                phiHelperModel.getVarVersionInBranch().put(var, varNumbers.get(var));
            }
            phiHelperModel.setBranch(ssaBlock);
            phiModels.add(phiHelperModel);

         }
    }

    private Set<String> getVarsFromBranch(SSABlock block) {
        if (block.getType() == CONDITION) throw new UnsupportedOperationException();
        SSAExpression expression = block.getExpression();
        Set<String> vars = getVarsFromExpression(expression);
        block.getChildren().forEach(child -> vars.addAll(getVarsFromBranch(child)));
        return vars;
    }

    private Set<String> getVarsFromExpression(SSAExpression expression) {
        HashSet<String> vars = new HashSet<>();

        SSAExpression left = expression.getLeft();
        String varName = left.getVarName();
        if (varName != null) vars.add(varName);
        else if (left.getNumericValue() == null) {
            vars.addAll(getVarsFromExpression(left));
        }
        return vars;
    }

    private void resolveVersionedVars(SSAExpression ssaBlockExpression) {
        String varName = ssaBlockExpression.getVarName();
        String numericValue = ssaBlockExpression.getNumericValue();
        if (varName != null) {

            for (PhiHelperModel phiModel : phiModels) {
                Integer versionsInBranch = phiModel.getVarVersionInBranch().get(varName);
                if (versionsInBranch != null) {
                    int versionOfPhi = Optional.ofNullable(varNumbers.get(varName)).orElse(1);
                    phiModel.putToPhiFunctions(varName, versionOfPhi);
                    varNumbers.put(varName, versionOfPhi + 2);
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
