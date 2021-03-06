package interfaceApplication;

import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.session.session;
import common.java.time.TimeHelper;

public class ContentRecord {
    private GrapeTreeDBModel Record;
    private GrapeDBSpecField gDbSpecField;
    private session se;
    private JSONObject userInfo = null;
    private String currentUser = null;

    public ContentRecord() {

        Record = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("ContentRecord"));
        Record.descriptionModel(gDbSpecField);
        Record.bindApp();
        
        se = new session();
        userInfo = se.getDatas();
        if (userInfo != null && userInfo.size() != 0) {
            currentUser = userInfo.getMongoID("_id"); // 当前用户id
        }
    }

    /**
     * 新增访问记录
     * @param oid
     * @return
     */
    @SuppressWarnings("unchecked")
    public String AddReader(String oid) {
        Object tip = null;
        String result = rMsg.netMSG(100, "新增失败");
        JSONObject info = new JSONObject();
        info.put("uid", currentUser); // 用户id
        info.put("oid", oid); // 文章id
        info.put("time", TimeHelper.nowMillis()); // 浏览时间
        tip = Record.data(info).insertEx();
        return tip!=null ? rMsg.netMSG(0, "新增成功"):result;
    }
}
