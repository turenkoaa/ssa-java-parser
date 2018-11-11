package com.kspt.aaturenko.parser;

import com.kspt.aaturenko.ssa_tree.SSABlock;
import com.kspt.aaturenko.ssa_tree.SSAExpression;

import java.util.*;
import java.util.stream.Collectors;

import static com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType.*;

/**
 * Created by Anastasia on 05.11.2018.
 */
public class VersionResolver {
    private Map<String, Integer> varNumbers = new HashMap<>();
    private Map<Long, PhiModel> phiModels = new HashMap<>();

    public void resolveVersions(SSABlock ssaBlock){
        resolve(ssaBlock);
        printPhiFunctions();
    }

    private void printPhiFunctions() {
        for (PhiModel phiModel : phiModels.values()) {
            for (String var : phiModel.getPhiFunctions()) {
                List<Integer> versions = phiModel.getVarVersionInBranch().get(var);
                int max = versions.stream().mapToInt(v -> v).max().getAsInt() + 1;
                String arg = versions.stream()
                        .map(version -> var + "_" + version)
                        .collect(Collectors.joining(","));

                System.out.println(var + "_" + max + " = phi(" + arg + ")");
            }
        }
    }

    public String getPhiExpr(long id) {
        PhiModel phiModel = phiModels.get(id);
        String phi = "";
        for (String var : phiModel.getPhiFunctions()) {
            List<Integer> versions = phiModel.getVarVersionInBranch().get(var);
            int max = versions.stream().mapToInt(v -> v).max().getAsInt() + 1;
            String arg = versions.stream()
                    .map(version -> var + "_" + version)
                    .collect(Collectors.joining(","));

            phi = phi + var + "_" + max + " = phi(" + arg + ") ";
        }
        return phi;
    }

    private void resolve(SSABlock ssaBlock) {
        if (ssaBlock.getType() != PHI) {
            SSAExpression ssaBlockExpression = ssaBlock.getExpression();
            String declaredVar = ssaBlockExpression.getLeft().getVarName();
            Integer version = varNumbers.get(declaredVar);

            resolveVersionedVars(ssaBlockExpression);

            if (ssaBlock.getType() == ASSIGNMENT) {
                varNumbers.put(declaredVar, version != null ? version + 1 : 1);
                ssaBlockExpression.getLeft().setVarName(declaredVar + "_" + varNumbers.get(declaredVar));
                ssaBlock.getChildren().forEach(this::resolve);
            } else if (ssaBlock.getType() == CONDITION) {
                int index = 0;
                PhiModel phiModel = new PhiModel();
                for (SSABlock branch : ssaBlock.getChildren()) {
                    for (String var : getVarsFromBranch(branch)) {
                        phiModel.putToVarVersionInBranch(var, varNumbers.get(var) + 1, index);
                    }
                    index++;
                    resolve(branch);
                }
                phiModels.put(ssaBlock.getId(), phiModel);
            }
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

            for (PhiModel phiModel : phiModels.values()) {
                List<Integer> versions = phiModel.getVarVersionInBranch().get(varName);
                if (versions != null && versions.size() > 1) {
                    int versionOfPhi = Optional.ofNullable(varNumbers.get(varName)).orElse(1);
                    phiModel.addToPhiFunctions(varName);
                    varNumbers.put(varName, versionOfPhi + 1);
                }
            }

            ssaBlockExpression.setVarName(varName + "_" + varNumbers.get(varName));
        } else if (numericValue == null) {
            resolveVersionedVars(ssaBlockExpression.getLeft());
            resolveVersionedVars(ssaBlockExpression.getRight());
        }
    }
}
