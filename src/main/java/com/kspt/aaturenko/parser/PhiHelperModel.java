package com.kspt.aaturenko.parser;

import com.kspt.aaturenko.ssa_tree.SSABlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anastasia on 07.11.2018.
 */
public class PhiHelperModel {
    Map<String, Integer> phiFunctions = new HashMap<>();
    Map<String, Integer> varVersionInBranch = new HashMap<>();
    SSABlock branch;
    SSABlock endOfBranch;

    public void putToPhiFunctions(String var, Integer version) {
        phiFunctions.put(var, version);
    }

    public void putToVarVersionInBranch(String var, Integer version) {
        varVersionInBranch.put(var, version);
    }

    public Map<String, Integer> getVarVersionInBranch() {
        return varVersionInBranch;
    }

    public SSABlock getBranch() {
        return branch;
    }

    public SSABlock getEndOfBranch() {
        return endOfBranch;
    }
}
