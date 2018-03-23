package org.cpqd.iotagent;

import com.google.gson.JsonPrimitive;
import org.eclipse.leshan.core.model.ResourceModel;

public class DeviceAttribute {

    public ResourceModel getResourceModel()


    private ResourceModel.Type getTypeFor(String valueType) {
        switch (valueType) {
            case "bool":
                return ResourceModel.Type.BOOLEAN;
            case "string":
                return ResourceModel.Type.STRING;
            case "float":
                return ResourceModel.Type.FLOAT;
            case "integer":
                return ResourceModel.Type.INTEGER;
            case "geo":
                return ResourceModel.Type.STRING;
            default:
                throw new IllegalArgumentException("Invalid value_type " + valueType);
        }
    }

    private ResourceModel.Operations getOpsFor(String type){
        switch (type) {
            case "dynamic":
                return ResourceModel.Operations.RW;
            case "static":
                return ResourceModel.Operations.R;
            case "meta":
                return ResourceModel.Operations.NONE;
            default:
                throw new IllegalArgumentException("Invalid type " + type);
        }
    }


}

