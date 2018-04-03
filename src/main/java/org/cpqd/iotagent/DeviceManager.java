package org.cpqd.iotagent;

import com.eclipsesource.json.Json;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.eclipse.leshan.Link;
import org.eclipse.leshan.LwM2mId;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.server.registration.Registration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.util.*;

public class DeviceManager {

    private String deviceUrl;
    private DinamicModelProvider modelProvider;
    private Map<String, String> paths = new HashMap<>();
    private Map<String, Registration> Devices = new HashMap<String, Registration>();
    private Map<String, String> Lwm2mDevices = new HashMap<String, String>();

//    BiMap<String, String> Lwm2mDevices = HashBiMap.create();



    // Todo(jsiloto): This would be nicer as a JsonDeserializer for generic attributes
    public static String getStaticValue(String label, JSONObject data) {
        // Get device label and new FW Version
        String value = "";
        data = data.getJSONObject("attrs");
        Iterator<?> templates = data.keys();
        while (templates.hasNext()) {
            String template = (String) templates.next();
            JSONArray attrs = data.getJSONArray(template);
            for (int i = 0; i < attrs.length(); i++) {
                JSONObject attr = (JSONObject) attrs.get(i);
                if (attr.getString("label").equals(label)) {
                    value = attr.getString("static_value");
                }
            }
        }
        return value;
    }


    // TODO(jsiloto): Isolate attribute

    // TODO(jsiloto): Check if attribute has metadata

    // TODO(jsiloto): Check if attribute has lwm2m metadata

    // TODO(jsiloto): Parse path

    // TODO(jsiloto): Check if Object exists

    // TODO(jsiloto): If doesn't exist deserialize object into resources

    // TODO(jsiloto): Update Lwm2mModel


    LinkedList<DeviceAttribute> getAttributes(JsonElement device) {
        LinkedList<DeviceAttribute> attributes = new LinkedList<>();
        // Get all ResourceModels for each attribute
        JsonObject data = device.getAsJsonObject().get("attrs").getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = data.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            for (JsonElement attr : entry.getValue().getAsJsonArray()) {
                System.out.println(attr);
                attributes.add(new DeviceAttribute(attr));
            }
        }
        return attributes;
    }

    public void RegisterModel(Device device) {
        Map<Integer, LinkedList<ResourceModel>> newModels = new HashMap<Integer, LinkedList<ResourceModel>>();
        LinkedList<ResourceModel> resources;

        String deviceLabel = device.label;

        // Get all ResourceModels for each attribute
        for (DeviceAttribute attr : device.attributes) {
            if (attr.isLwm2mAttr()) {
                ResourceModel attrModel = attr.getLwm2mResourceModel();
                int objectId = attr.getLwm2mPath()[0];
                if (!newModels.containsKey(objectId)) {
                    paths.put(attr.path, attr.label);
                    newModels.put(objectId, new LinkedList<ResourceModel>());
                }
                newModels.get(objectId).add(attr.getLwm2mResourceModel());
            }
        }

        // TODO(jsiloto): Should models be updated everytime?
        // Iterate over discovered models, add if not already in the provider
        for (Map.Entry<Integer, LinkedList<ResourceModel>> entry : newModels.entrySet()) {
            int resourceId = entry.getKey();
            LwM2mModel model = modelProvider.getObjectModel(null);
            ObjectModel oldModel = model.getObjectModel(resourceId);
            //TODO(jsiloto): Can't we just update the model with new attributes?
            if (oldModel == null) {
                ObjectModel objectModel = new ObjectModel(resourceId, deviceLabel,
                        "", "1", false, false, entry.getValue());
                modelProvider.addObjectModel(objectModel);
            }
        }
    }


    public DeviceManager(String deviceManagerUrl, DinamicModelProvider modelProvider) {
        this.deviceUrl = deviceManagerUrl + "/device";
        this.modelProvider = modelProvider;
    }

    public JsonElement GetDeviceFromDeviceManager(String service, String deviceModel, String serialNumber) {
        String token = TenancyManager.GetJwtToken(service);
        String query = "?attr=device_type=" + deviceModel + "&serial_number=" + serialNumber;
        String url = this.deviceUrl + query;

        try {
            HttpResponse<JsonNode> response = Unirest.get(url).header("Authorization", "Bearer " + token).asJson();
            if (response.getStatus() >= 300) {
                return null;
            }
            JsonNode r = response.getBody();
            JSONArray devices = r.getObject().getJSONArray("devices");
            if (devices.length() == 0) {
                return null;
            }
            JSONObject device = devices.getJSONObject(0);
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonDevice = (JsonObject) jsonParser.parse(device.toString());
            return gsonDevice;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return null;
    }

    public void RegisterDevice(Device device, String service, String lwm2mId, String deviceModel, String serialNumber, Registration registration) {
        RegisterModel(device);
        Devices.put(device.deviceId, registration);
        Lwm2mDevices.put(lwm2mId, device.deviceId);
        System.out.println(device.deviceId);
    }

    public Registration getDeviceRegistration(String id) {
        return Devices.get(id);
    }

    public Registration getLwm2mRegistration(String id) {
        return Devices.get(Lwm2mDevices.get(id));
    }

    public String getDeviceId(String lwm2mId){
        return Lwm2mDevices.get(lwm2mId);
    }

    public void DeregisterDevice(String lwm2mId) {
        String deviceId = Lwm2mDevices.get(lwm2mId);
        Devices.remove(deviceId);
        Lwm2mDevices.remove(lwm2mId);
    }

    public String getLabelFromPath(String path){
        Integer[] ids = DeviceAttribute.getIdsfromPath(path);
        if(ids == null){
            return "";
        }
        return modelProvider.getObjectModel(null).getResourceModel(ids[0], ids[2]).name;

    }


}
