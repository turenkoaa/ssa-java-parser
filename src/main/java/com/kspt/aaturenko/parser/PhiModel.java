package com.kspt.aaturenko.parser;

import com.kspt.aaturenko.ssa_tree.SSABlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anastasia on 07.11.2018.
 */
public class PhiModel {
    private List<String>  phiFunctions = new ArrayList<>();
    private Map<String, List<Integer>> varVersionInBranch = new HashMap<>();

    public void addToPhiFunctions(String var) {
        phiFunctions.add(var);
    }

    public void putToVarVersionInBranch(String var, Integer version, Integer index) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(version);

        varVersionInBranch.merge(var, list, (v1, v2) -> {
            if (index >= v1.size()) {
                v1.add(index, version);
            } else {
                v1.set(index, version);
            }
            return v1;
        });
    }

    public Map<String, List<Integer>> getVarVersionInBranch() {
        return varVersionInBranch;
    }

    public List<String> getPhiFunctions() {
        return phiFunctions;
    }

}
