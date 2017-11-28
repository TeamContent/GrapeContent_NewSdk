package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.star.io.TempFile;

import apps.appsProxy;
import cache.CacheHelper;
import check.checkHelper;
import database.dbFilter;
import httpClient.request;
import interfaceApplication.ContentGroup;
import json.JSONHelper;
import nlogger.nlogger;
import security.codec;
import session.session;
import string.StringHelper;

public class CommonModel {
    private String APIHost = "";
    private String APIAppid = "";
    private String appid = appsProxy.appidString();
    private session se;
    private JSONObject userInfo = null;
    private String userID = null;
    private String userName = null;

    private final Pattern ATTR_PATTERN = Pattern.compile("<img[^<>]*?\\ssrc=['\"]?(.*?)['\"]?\\s.*?>", Pattern.CASE_INSENSITIVE);

    public CommonModel() {
        session se = new session();
        userInfo = se.getDatas();
        if (userInfo != null && userInfo.size() > 0) {
            userID = userInfo.getString("id");
            userName = userInfo.getString("name");
        }
    }

    /**
     * 设置栏目模版到文章信息
     * 
     * @param array
     * @return
     */
    @SuppressWarnings("unchecked")
    public JSONArray setTemplate(JSONArray array) {
        JSONObject object;
        String[] values;
        String value, list = "", content = "", temp;
        array = ContentDencode(array); // 解码
        if (array == null || array.size() <= 0) {
            return array;
        }
        JSONObject tempObj = getTemplate(array);
        if (tempObj != null && tempObj.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                object = (JSONObject) array.get(i);
                value = object.getString("ogid");
                if (tempObj != null && tempObj.size() != 0) {
                    temp = tempObj.getString(value);
                    if (StringHelper.InvaildString(temp)) {
                        values = temp.split(",");
                        content = values[0];
                        list = values[1];
                    }
                }
                object.put("TemplateContent", content);
                object.put("Templatelist", list);
                array.set(i, object);
            }
        }
        return array;
    }

    /**
     * 获取栏目模版信息
     * 
     * @param array
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject getTemplate(JSONArray array) {
        JSONObject object;
        String id = "", temp, column;
        String TemplateContent, Templatelist, tid;
        JSONObject tempObj = new JSONObject();
        if (array != null && array.size() >= 0) {
            for (Object obj : array) {
                object = (JSONObject) obj;
                temp = object.getString("ogid");
                if (!id.contains(temp)) {
                    id += temp + ",";
                }
            }
        }
        if (StringHelper.InvaildString(id)) {
            id = StringHelper.fixString(id, ',');
            column = new ContentGroup().getGroupById(id);
            array = JSONArray.toJSONArray(column);
        }
        if (array != null && array.size() != 0) {
            int l = array.size();
            for (int i = 0; i < l; i++) {
                object = (JSONObject) array.get(i);
                if (object != null && object.size() != 0) {
                    TemplateContent = object.getString("TemplateContent");
                    Templatelist = object.getString("TemplateList");
                    tid = object.getString("_id");
                    if (!TemplateContent.equals("") && !Templatelist.equals("")) {
                        tempObj.put(tid, TemplateContent + "," + Templatelist);
                    }
                }
            }
        }
        return tempObj;
    }

    /**
     * 发送数据到kafka
     * 
     * @param id
     * @param mode
     * @param newstate
     */
    public void setKafka(String id, int mode, int newstate) {
        APIHost = getconfig("APIHost");
        if (!APIHost.equals("") && !APIAppid.equals("")) {
            request.Get(APIHost + "/sendServer/ShowInfo/getKafkaData/" + id + "/" + appid + "/int:1/int:" + mode + "/int:" + newstate);
        }
    }

    /**
     * 添加操作日志
     * 
     * @param type
     *            类型 0:新增XXX栏目；1：删除XXX栏目；2：更新XXX栏目；3：查询栏目信息，查询条件为：
     *            4：在XXX栏目下新增XXX文章；5：更新XXX栏目下XXX文章；6：删除XXX栏目下的XXX文章；7：查询栏目信息，
     *            查询条件为：
     * @param _id
     *            操作对象- _id
     * @param func
     *            调用接口
     * @param condString
     *            查询条件
     */
    public void AddLog(int type, String obj, String func, String condString) {
        String action = "";
        String columnName = getColumnName(obj);
        switch (type) {
        case 0: // 新增XXX栏目
            action = "新增[" + columnName + "]栏目";
            break;
        case 1: // 删除XXX栏目
            action = "删除[" + columnName + "]栏目," + condString;
            break;
        case 2: // 更新XXX栏目
            action = "更新[" + columnName + "]栏目," + condString;
            break;
        case 3: // 在XXX栏目下新增XXX文章

            break;
        case 4: // 更新XXX栏目下XXX文章

            break;
        case 5: // 删除XXX栏目下的XXX文章

            break;
        case 6: // 审核文章

            break;

        default:
            break;
        }
        appsProxy.proxyCall("/GrapeLog/Logs/AddLogs/" + userID + "/" + userName + "/" + action + "/" + func, appsProxy.getCurrentAppInfo());
    }

    /**
     * 获取栏目名称
     * 
     * @param ogid
     * @return
     */
    private String getColumnName(String ogid) {
        String columnName = ogid;
        if (StringHelper.InvaildString(ogid) && !ogid.equals("0")) {
            if (ObjectId.isValid(ogid) || checkHelper.isInt(ogid)) {
                JSONObject temp = JSONObject.toJSON(new ContentGroup().getColumnName(ogid));
                if (temp != null && temp.size() > 0) {
                    columnName = temp.getString(ogid);
                } else {
                    columnName = "";
                }
            }
        }
        return columnName;
    }

    /**
     * 获取栏目名称，文章名称
     * 
     * @param ogid
     * @return
     */
    private String getArticleName(String id) {
        String columnName = "";
        return columnName;
    }

    /**
     * 获取配置信息
     * 
     * @param key
     * @return
     */
    private String getconfig(String key) {
        String value = "";
        try {
            JSONObject object = JSONObject.toJSON(appsProxy.configValue().getString("other"));
            if (object != null && object.size() > 0) {
                value = object.getString(key);
            }
        } catch (Exception e) {
            nlogger.logout(e);
            value = "";
        }
        return value;
    }

    /**
     * 获取图片相对地址
     * 
     * @param imageURL
     * @return
     */
    public String getImageUri(String imageURL) {
        int i = 0;
        if (imageURL.contains("File//upload")) {
            i = imageURL.toLowerCase().indexOf("file//upload");
            imageURL = "\\" + imageURL.substring(i);
        }
        if (imageURL.contains("File\\upload")) {
            i = imageURL.toLowerCase().indexOf("file\\upload");
            imageURL = "\\" + imageURL.substring(i);
        }
        if (imageURL.contains("File/upload")) {
            i = imageURL.toLowerCase().indexOf("file/upload");
            imageURL = "\\" + imageURL.substring(i);
        }
        return imageURL;
    }

    /**
     * 参数解码
     * 
     * @param param
     *            按 base64 + 特殊格式 顺序 编码后的参数
     * @return
     */
    public String dencode(String param) {
        if (param != null && !param.equals("") && !param.equals("null")) {
            param = codec.DecodeHtmlTag(param);
            param = codec.decodebase64(param);
        }
        return param;
    }

    /**
     * 整合参数，将JSONObject类型的参数封装成JSONArray类型
     * 
     * @param object
     * @return
     */
    public JSONArray buildCond(String Info) {
        String key;
        Object value;
        JSONArray condArray = null;
        JSONObject object = JSONObject.toJSON(Info);
        dbFilter filter = new dbFilter();
        if (object != null && object.size() > 0) {
            for (Object object2 : object.keySet()) {
                key = object2.toString();
                value = object.get(key);
                filter.eq(key, value);
            }
            condArray = filter.build();
        } else {
            condArray = JSONArray.toJSONArray(Info);
        }
        return condArray;
    }

    /**
     * 获取当前网站所有下级网站id，包含自身网站
     * 
     * @project GrapeContent
     * @package model
     * @file ContentGroupModel.java
     * 
     * @param wbid
     * @return
     *
     */
    public String[] getWeb(String wbid) {
        String[] value = null;
        wbid = getRWbid(wbid);
        String temp = (String) appsProxy.proxyCall("/GrapeWebInfo/WebInfo/getWebTree/" + wbid);// 获取下级网站
        if (temp != null && !temp.equals("")) {
            value = temp.split(",");
        }
        return value;
    }

    /**
     * 获取虚站对应的实战id
     * 
     * @param wbid
     * @return
     */
    public String getRWbid(String wbid) {
        String value = wbid;
        CacheHelper cacheHelper = new CacheHelper();
        String key = "vID2rID_" + wbid;
        Object obj = (String) appsProxy.proxyCall("/GrapeWebInfo/WebInfo/VID2RID/" + wbid);
        if (obj != null) {
            value = obj.toString();
            if (StringHelper.InvaildString(value)) {
                cacheHelper.setget(key, value, 86400);
            }
        }
        return value;
    }

    /**
     * 文章内容编码，图片地址设置
     * 
     * @param object
     */
    @SuppressWarnings("unchecked")
    public JSONObject ContentEncode(JSONObject object) {
        String temp = "";
        if (object != null && object.size() > 0) {
            if (object.containsKey("content")) {
                object.escapeHtmlPut("content", object.getString("content"));
            }
            if (object.containsKey("image")) {
                temp = object.getString("image");
                if (temp != null && !temp.equals("") && !temp.equals("null")) {
                    temp = codec.DecodeHtmlTag(temp);
                    object.put("image", RemoveUrlPrefix(temp));
                }
            }
        }
        return object;
    }

    /**
     * 文章内容编码
     * 
     * @param object
     */
    @SuppressWarnings("unchecked")
    public JSONArray ContentDencode(JSONArray array) {
        JSONObject object;
        if (array != null && array.size() > 0) {
            int l = array.size();
            for (int i = 0; i < l; i++) {
                object = (JSONObject) array.get(i);
                object = ContentDencode(object);
                array.set(i, object);
            }
        }
        return array;
    }

    /**
     * 文章内容编码
     * 
     * @param object
     */
    @SuppressWarnings("unchecked")
    public JSONObject ContentDencode(JSONObject object) {
        if (object != null && object.size() > 0) {
            if (object.containsKey("content")) {
                object.put("content", object.escapeHtmlGet("content"));
            }
        }
        return object;
    }

    /**
     * 删除图片前缀
     * 
     * @project GrapeContent
     * @package model
     * @file ContentModel.java
     * 
     * @param imgUrl
     * @return
     *
     */
    public String RemoveUrlPrefix(String imageUrl) {
        String image = "";
        if (imageUrl.equals("") || imageUrl == null) {
            return imageUrl;
        }
        String[] imgUrl = imageUrl.split(",");
        for (String string : imgUrl) {
            if (string.contains("http://")) {
                string = getImageUri(string);
            }
            image += string + ",";
        }
        return StringHelper.fixString(image, ',');
    }

    /**
     * 显示文章时，文章中的图片地址，图片内容文章的图片地址添上前缀 文章内容为解码后的内容
     * 
     * @project GrapeContent
     * @package model
     * @file ContentModel.java
     * 
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    public JSONArray getImgs(JSONArray array) {
        JSONObject object;
        if (array == null || array.size() <= 0) {
            return new JSONArray();
        }
        for (int i = 0; i < array.size(); i++) {
            object = (JSONObject) array.get(i);
            object = getImgs(object);
            array.set(i, object);
        }
        // String id;
        // for (int i = 0; i < array.size(); i++) {
        // object = (JSONObject) array.get(i);
        // id = object.getString("_id");
        // imgobj.put(id, object.get("image"));
        // conobj.put(id, object.get("content"));
        // }
        // imgobj = getImage(imgobj);
        // conobj = getContentImgs(conobj);
        // for (int i = 0; i < array.size(); i++) {
        // object = (JSONObject) array.get(i);
        // id = object.getString("_id");
        // object.put("image", imgobj.get(id));
        // object.put("content", conobj.get(id));
        // array.set(i, object);
        // }
        return array;
    }

    /**
     * 解析文章图片,内容图片
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public JSONObject getImgs(JSONObject object) {
        String id;
        JSONObject imgobj = new JSONObject();
        JSONObject conobj = new JSONObject();
        if (object == null || object.size() == 0) {
            return new JSONObject();
        }
        id = object.getMongoID("_id");
        imgobj.put(id, object.get("image"));
        conobj.put(id, object.get("content"));
        imgobj = getImage(imgobj);
        conobj = getContentImgs(conobj);
        object.put("content", conobj.get(id));
        object.put("image", imgobj.get(id));
        return object;
    }

    /**
     * 获取默认缩略图
     * 
     * @project GrapeContent
     * @package interfaceApplication
     * @file Content.java
     * 
     * @param wbid
     * @param array
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    public JSONArray getDefaultImage(String wbid, JSONArray array) {
        CacheHelper ch = new CacheHelper();
        String thumbnail = "", suffix = "", tempString = "0", temp;
        int type = 0;
        if (!wbid.equals("") && array != null && array.size() != 0) {
            int l = array.size();
            // 显示默认缩略图
            temp = ch.get("DefaultImage_" + wbid);
            if (temp == null) {
                temp = (String) appsProxy.proxyCall("/GrapeWebInfo/WebInfo/getImage/" + wbid);
                ch.setget("DefaultImage_" + wbid, temp, 86400);
            }
            JSONObject Obj = JSONObject.toJSON(temp);
            if (Obj != null && Obj.size() != 0) {
                if (Obj.containsKey("thumbnail")) {
                    thumbnail = Obj.getString("thumbnail");
                }
                if (Obj.containsKey("suffix")) {
                    suffix = Obj.getString("suffix");
                }
            }
            for (int i = 0; i < l; i++) {
                Obj = (JSONObject) array.get(i);
                if (Obj != null && Obj.size() > 0) {
                    if (Obj.containsKey("isSuffix")) {
                        tempString = Obj.getString("isSuffix");
                        if (tempString.contains("$numberLong")) {
                            tempString = JSONObject.toJSON(tempString).getString("$numberLong");
                        }
                        tempString = (tempString == null || tempString.equals("") || tempString.equals("null")) ? "0" : tempString;
                        type = Integer.parseInt(tempString);
                    }
                    if (type == 0) {
                        suffix = "";
                    }
                    Obj.put("thumbnail", thumbnail);
                    Obj.put("suffix", suffix);
                }
                array.set(i, Obj);
            }
        }
        return array;
    }

    /**
     * 图片地址
     * 
     * @project GrapeContent
     * @package model
     * @file ContentModel.java
     * 
     * @param objimg
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    private JSONObject getImage(JSONObject objimg) {
        String key, value;
        for (Object obj : objimg.keySet()) {
            key = obj.toString();
            value = objimg.getString(key);
            if (!value.contains("http://")) {
                objimg.put(key, AddUrlPrefix(value));
            }
        }
        return objimg;
    }

    /**
     * 添加图片前缀
     * 
     * @project GrapeContent
     * @package model
     * @file ContentModel.java
     * 
     * @param imgUrl
     * @return
     *
     */
    private String AddUrlPrefix(String imageUrl) {
        if (imageUrl.equals("") || imageUrl == null) {
            return imageUrl;
        }
        String[] imgUrl = imageUrl.split(",");
        List<String> list = new ArrayList<>();
        for (String string : imgUrl) {
            if (string.contains("http:")) {
                string = getImageUri(string);
            }
            string = getconfig("fileHost") + string;
            list.add(string);
        }
        return StringHelper.join(list);
    }

    /**
     * 内容显示
     * 
     * @project GrapeContent
     * @package model
     * @file ContentModel.java
     * 
     * @param objcontent
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    private JSONObject getContentImgs(JSONObject objcontent) {
        String key, value;
        for (Object obj : objcontent.keySet()) {
            key = obj.toString();
            value = objcontent.getString(key);
            if (!value.equals("")) {
                Matcher matcher = ATTR_PATTERN.matcher(value.toLowerCase());
                int code = matcher.find() ? 0 : value.contains("/File/upload") ? 1 : 2;
                switch (code) {
                case 0: // 文章内容为html带图片类型的内容处理
                    value = AddHtmlPrefix(value);
                    break;
                case 1: // 文章内容图片类型处理[获取图片的相对路径]
                    value = AddUrlPrefix(value);
                    break;
                case 2: // 文章内容文字类型处理
                    break;
                default:
                    break;
                }
                objcontent.put(key, value);
            }
        }
        return objcontent;
    }

    /**
     * 添加html中图片地址 http://.....
     * 
     * @project GrapeContent
     * @package model
     * @file ContentModel.java
     * 
     * @param Contents
     *            若文章内容的图片含有http://,则直接返回该地址，否则新增地址http://...+imgurl
     * @return
     *
     */
    private String AddHtmlPrefix(String Contents) {
        String imgurl = "host";
        String temp = "", newurl = "";
        if (Contents != null && !Contents.equals("") && !Contents.equals("null")) {
            List<String> list = getCommonAddr(Contents); // 匹配后img标签中的值即Contents中的值
            if (list != null && list.size() > 0) {
                int l = list.size();
                for (int i = 0; i < l; i++) {
                    temp = list.get(i);
                    if (!temp.contains("http://")) {
                        if (!temp.startsWith("/") || !temp.startsWith("//") || !temp.startsWith("\\")) {
                            newurl = "\\" + temp;
                        }
                        newurl = imgurl + newurl;
                        Contents = Contents.replace(temp, newurl);
                    }
                }
            }
        }
        return Contents;
    }

    /**
     * 获取html内容中的图片地址集
     * 
     * @project GrapeContent
     * @package model
     * @file ContentModel.java
     * 
     * @param contents
     * @return
     *
     */
    private List<String> getCommonAddr(String contents) {
        Matcher matcher = ATTR_PATTERN.matcher(contents);
        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
            list.add(matcher.group(1));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public JSONArray join(JSONArray array) {
        array = getImgs(array);
        JSONObject object;
        if (array == null || array.size() <= 0) {
            return null;
        }
        try {
            int len = array.size();
            for (int i = 0; i < len; i++) {
                object = (JSONObject) array.get(i);
                object = join(object);
                array.set(i, object);
            }
        } catch (Exception e) {
            System.out.println("content.join:" + e.getMessage());
            array = null;
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public JSONObject join(JSONObject object) {
        JSONObject tmpJSON = object;
        if (tmpJSON != null) {
            if (!tmpJSON.containsKey("tempid")) {
            } else {
                tmpJSON.put("tempContent", getTemplate(tmpJSON.get("tempid").toString()));
            }
        }
        return tmpJSON;
    }

    /**
     * 获取模版数据
     * 
     * @param tid
     * @return
     */
    private String getTemplate(String tid) {
        String temp = "";
        CacheHelper cache = new CacheHelper();
        try {
            if (tid.contains("$numberLong")) {
                tid = JSONHelper.string2json(tid).getString("$numberLong");
            }
            if (!("0").equals(tid)) {
                if (cache.get(tid) != null) {
                    temp = cache.get(tid).toString();
                } else {
                    temp = appsProxy.proxyCall("/GrapeTemplate/TemplateContext/TempFindByTid/s:" + tid).toString();
                    cache.setget(tid, temp, 10 * 3600);
                }
            }
        } catch (Exception e) {
            nlogger.logout(e);
            temp = "";
        }
        return temp;
    }

    /**
     * 获取默认缩略图
     * 
     * @project GrapeContent
     * @package interfaceApplication
     * @file Content.java
     * 
     * @param wbid
     * @param array
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    public JSONObject getDefaultImage(JSONObject object) {
        CacheHelper ch = new CacheHelper();
        String thumbnail = "", temp;
        if (object != null && object.size() != 0) {
            String wbid = object.getString("wbid");
            // 显示默认缩略图
            temp = ch.get("DefaultImage_" + wbid);
            if (temp == null) {
                temp = (String) appsProxy.proxyCall("/GrapeWebInfo/WebInfo/getImage/" + wbid);
                ch.setget("DefaultImage_" + wbid, temp, 86400);
            }
            JSONObject Obj = JSONObject.toJSON(temp);
            if (Obj != null && Obj.size() != 0) {
                thumbnail = Obj.getString("thumbnail");
            }
            object.put("thumbnail", thumbnail);
        }
        return object;
    }

    /**
     * 获取默认缩略图
     * 
     * @project GrapeContent
     * @package interfaceApplication
     * @file Content.java
     * 
     * @param wbid
     * @param array
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    public JSONArray getDefaultImage(JSONArray array) {
        CacheHelper ch = new CacheHelper();
        String thumbnail = "", wbid = "", temp;
        JSONObject object, tempObj, Obj = new JSONObject();
        if (array != null && array.size() > 0) {
            int l = array.size();
            for (Object obj : array) {
                tempObj = (JSONObject) obj;
                temp = tempObj.getString("wbid");
                if (!wbid.contains(temp)) {
                    wbid += temp + ",";
                }
            }
            if (StringHelper.InvaildString(wbid)) {
                wbid = StringHelper.fixString(wbid, ',');
                temp = ch.get("DefaultImage_" + wbid);
                if (temp == null) {
                    temp = (String) appsProxy.proxyCall("/GrapeWebInfo/WebInfo/getImage/" + getRWbid(wbid));
                    ch.setget("DefaultImage_" + wbid, temp, 86400);
                }
                Obj = JSONObject.toJSON(temp);
            }
            if (Obj != null && Obj.size() > 0) {
                for (int i = 0; i < l; i++) {
                    object = (JSONObject) array.get(i);
                    wbid = object.getString("wbid");
                    thumbnail = Obj.getString(wbid);
                    thumbnail = JSONObject.toJSON(thumbnail).getString("thumbnail");
                    object.put("thumbnail", thumbnail);
                    array.set(i, object);
                }
            }
        }
        return array;
    }
}
