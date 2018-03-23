package org.cpqd.iotagent;

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

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class DeviceManager {

    private String deviceUrl;
    private Map<String, Registration> Devices = new HashMap<String, Registration>();
    private Map<String, String> Lwm2mDevices = new HashMap<String, String>();

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



    public void RegisterModel(JSONObject data) {


        LinkedList<ResourceModel> resources;


        ResourceModel resourceModel = new ResourceModel();

        LwM2mModel model = new LwM2mModel();



        data = data.getJSONObject("attrs");
        Iterator<?> templates = data.keys();
        while (templates.hasNext()) {
            String template = (String) templates.next();
            JSONArray attr_list = data.getJSONArray(template);
            for (int i = 0; i < attr_list.length(); i++) {
                JSONObject attr = (JSONObject) attr_list.get(i);

                // Check if attribute has Lwm2m path
                if(attr.has("metadata")){




                    if(attr.getJSONObject("metadata").has("path") && ){
                        path = attr.getJSONObject("metadata").getString("path");
                        String[] ids = path.split("/");


                        //Check if object exists in modelprovider if not create
                        //Check if resource exists in model
                        //update model
                    }
                }



                String resourceName = attr.getString("label");
                String valueType = attr.getString("value_type");
                String type = attr.getString("type");
                String path = "";




                JSONObject attr = (JSONObject) attr_list.get(i);
                if (attr.getString("label").equals(label)) {
                    value = attr.getString("static_value");
                }

                ResourceModel model = new ResourceModel(i, resourceName, ResourceModel.Operations.RW, false,
                        false, ResourceModel.Type.STRING, "", "", "");


            }
        }

        int newId = 5000;
        ObjectModel objectModel = new ObjectModel(newId, deviceLabel, "", false, false, );


        return value;


    }


    public DeviceManager(String deviceManagerUrl) {
        this.deviceUrl = deviceManagerUrl + "/device";
    }

    public void RegisterDevice(String service, String lwm2mId, String deviceModel, String serialNumber, Registration registration) {
        String token = TenancyManager.GetJwtToken(service);
        String query = "?attr=device_type=" + deviceModel + "&serial_number=" + serialNumber;
        String url = this.deviceUrl + query;

        try {
            HttpResponse<JsonNode> response = Unirest.get(url).header("Authorization", "Bearer " + token).asJson();
            if (response.getStatus() >= 300) {
                return;
            }
            JsonNode r = response.getBody();
            String id = r.getObject().getJSONArray("devices").getJSONObject(0).get("id").toString();
            Devices.put(id, registration);
            Lwm2mDevices.put(lwm2mId, id);
            System.out.println(id);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public Registration getDeviceRegistration(String id) {
        return Devices.get(id);
    }

    public Registration getLwm2mRegistration(String id) {
        return Devices.get(Lwm2mDevices.get(id));
    }

    public void DeregisterDevice(String lwm2mId) {
        String deviceId = Lwm2mDevices.get(lwm2mId);
        Devices.remove(deviceId);
        Lwm2mDevices.remove(lwm2mId);
    }


}
