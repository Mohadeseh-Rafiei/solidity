package org.example;

import java.util.Objects;

public class MCRL2ContractMapping {
    private final MCRL2Node contract;
    private final String mappingType;
    private final String mappedType;

    public MCRL2ContractMapping(MCRL2Node contract, String mappingType, String mappedType) {
        this.contract = contract;
        this.mappingType = mappingType;
        this.mappedType = mappedType;
    }

    public boolean isEqual(MCRL2ContractMapping anotherMapping) {
        return anotherMapping.contract == this.contract && Objects.equals(anotherMapping.mappedType, this.mappedType) && Objects.equals(anotherMapping.mappingType, this.mappingType);
    }

    public MCRL2Node getContract() {
        return contract;
    }

    public String getMappedType() {
        return mappedType;
    }

    public String getMappingType() {
        return mappingType;
    }
}
