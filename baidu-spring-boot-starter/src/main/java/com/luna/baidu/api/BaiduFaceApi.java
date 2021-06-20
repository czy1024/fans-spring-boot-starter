package com.luna.baidu.api;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.luna.baidu.constant.ImageConstant;
import com.luna.baidu.dto.face.*;
import com.luna.baidu.req.face.FaceLiveReq;
import com.luna.common.file.FileTools;
import com.luna.common.net.HttpUtils;
import com.luna.common.net.HttpUtilsConstant;
import com.luna.common.text.Base64Util;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;

/**
 * @author Luna@win10
 * @date 2020/4/20 11:46
 */
public class BaiduFaceApi {

    private static final Logger log = LoggerFactory.getLogger(BaiduFaceApi.class);

    /**
     * 人脸识别Api 返回face_token
     *
     * @param base64Str
     * @return List<Face>
     * @throws IOException
     */
    public static FaceCheckResultDTO faceDetect(String key, String base64Str) {
        log.info("faceDetect start");
        HttpResponse httpResponse = HttpUtils.doPost(BaiduApiConstant.HOST, BaiduApiConstant.FACE,
            ImmutableMap.of("Content-Type", HttpUtilsConstant.JSON), ImmutableMap.of("access_token", key),
            JSON.toJSONString(ImmutableMap.of("image_type", "BASE64", "max_face_num", "10",
                "image", base64Str)));
        String s = HttpUtils.checkResponseAndGetResult(httpResponse, true);
        FaceCheckResultDTO faceCheckResultDTO =
            JSON.parseObject(JSON.parseObject(s).getString("result"), FaceCheckResultDTO.class);
        log.info("faceDetect success faceCheckResultDTO={}", faceCheckResultDTO);
        return faceCheckResultDTO;
    }

    /**
     * 人脸对比
     *
     * @param key
     * 图片类型
     * BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；
     * URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；
     * FACE_TOKEN: 人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。
     * @param live1
     * 图片信息(总数据大小应小于10M)，图片上传方式根据image_type来判断。 两张图片通过json格式上传，格式参考表格下方示例
     * @param imageType1
     * 人脸的类型
     * LIVE：表示生活照：通常为手机、相机拍摄的人像图片、或从网络获取的人像图片等，
     * IDCARD：表示身份证芯片照：二代身份证内置芯片中的人像照片，
     * WATERMARK：表示带水印证件照：一般为带水印的小图，如公安网小图
     * CERT：表示证件照片：如拍摄的身份证、工卡、护照、学生证等证件图片
     * INFRARED 表示红外照片：使用红外相机拍摄的照片
     * 默认LIVE
     * @param live2
     * @param imageType2
     * @return
     */
    public static FaceMatchResultDTO faceMatch(String key, String live1, String imageType1, String faceType1,
        String live2,
        String imageType2, String faceType2) {
        log.info("faceMatch start");

        ImmutableMap<String, String> face1 =
            ImmutableMap.of("image", live1, "image_type", imageType1, "face_type", faceType1);
        ImmutableMap<String, String> face2 =
            ImmutableMap.of("image", live2, "image_type", imageType2, "face_type", faceType2);
        String json = "[" + JSON.toJSONString(face1) + "," + JSON.toJSONString(face2) + "]";
        HttpResponse httpResponse =
            HttpUtils.doPost(BaiduApiConstant.HOST, BaiduApiConstant.MATCH,
                ImmutableMap.of("Content-Type", HttpUtilsConstant.JSON), ImmutableMap.of("access_token", key), json);
        String s = HttpUtils.checkResponseAndGetResult(httpResponse, true);
        FaceMatchResultDTO result = JSON.parseObject(JSON.parseObject(s).getString("result"), FaceMatchResultDTO.class);
        log.info("faceMatch success result={}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 人脸对比
     *
     * @param live 脸部生活照
     * @param idCard 身份证照片
     * @return 比较数值
     * @throws IOException
     */
    public static FaceMatchResultDTO faceMathch(String key, String live, Integer liveImageType, String idCard,
        Integer idCardImageType) {
        return faceMatch(key, live, ImageConstant.getTypeStr(liveImageType), "LIVE", idCard,
            ImageConstant.getTypeStr(idCardImageType), "IDCARD");
    }

    public static FaceMatchResultDTO faceMathchWithBase64(String key, String live, String idCard) {
        return faceMatch(key, live, ImageConstant.IMAGE_BASE.getImageStr(), "LIVE", idCard,
            ImageConstant.IMAGE_BASE.getImageStr(), "IDCARD");
    }

    public static FaceMatchResultDTO faceMathchWithURL(String key, String live, String idCard) {
        return faceMatch(key, live, ImageConstant.IMAGE_URL.getImageStr(), "LIVE", idCard,
            ImageConstant.IMAGE_URL.getImageStr(), "IDCARD");
    }

    public static FaceMatchResultDTO faceMathchWithFaceToken(String key, String live, String idCard) {
        return faceMatch(key, live, ImageConstant.FACE_TOKEN.getImageStr(), "LIVE", idCard,
            ImageConstant.FACE_TOKEN.getImageStr(), "IDCARD");
    }

    /**
     * 活体检测
     * 
     * @param key
     * @param faceField
     * @return
     */
    public static FaceLiveResultDTO checkLive(String key, List<FaceLiveReq> faceField) {
        log.info("checkLive start faceField={}", JSON.toJSONString(faceField));
        HttpResponse httpResponse =
            HttpUtils.doPost(BaiduApiConstant.HOST, BaiduApiConstant.LIVE,
                ImmutableMap.of("Content-Type", HttpUtilsConstant.JSON), ImmutableMap.of("access_token", key),
                JSON.toJSONString(faceField));
        String s = HttpUtils.checkResponseAndGetResult(httpResponse, true);
        FaceLiveResultDTO faceLiveResultDTO =
            JSON.parseObject(JSON.parseObject(s).getString("result"), FaceLiveResultDTO.class);
        log.info("checkLive success faceLiveResultDTO={}", JSON.toJSONString(faceLiveResultDTO));
        return faceLiveResultDTO;
    }

    /**
     * 单张活体检测
     * 
     * @param key
     * @param image
     * @return
     */
    public static FaceLiveResultDTO checkLiveWithBase64(String key, String image) {
        return checkLive(key, Lists.newArrayList(
            new FaceLiveReq(image, ImageConstant.IMAGE_BASE.getImageStr(), "age,beauty,spoofing", "COMMON")));
    }

    public static FaceLiveResultDTO checkLiveWithUrl(String key, String image) {
        return checkLive(key, Lists.newArrayList(
            new FaceLiveReq(image, ImageConstant.IMAGE_URL.getImageStr(), "age,beauty,spoofing", "COMMON")));
    }

    public static FaceLiveResultDTO checkLiveWithFaceToken(String key, String image) {
        return checkLive(key, Lists.newArrayList(
            new FaceLiveReq(image, ImageConstant.FACE_TOKEN.getImageStr(), "age,beauty,spoofing", "COMMON")));
    }

    /**
     * 多张活体检测 base64 编码
     * 
     * @param key
     * @param image
     */
    public static FaceLiveResultDTO checkLiveWithBase64(String key, Set<String> image) {
        List<FaceLiveReq> faceLiveReqs = image.stream()
            .map(img -> new FaceLiveReq(img, ImageConstant.IMAGE_BASE.getImageStr(), "age,beauty,spoofing", "COMMON"))
            .collect(Collectors.toList());

        return checkLive(key, faceLiveReqs);
    }

    /**
     * 多张活体检测 url链接
     *
     * @param key
     * @param image
     */
    public static FaceLiveResultDTO checkLiveWithUrl(String key, Set<String> image) {
        List<FaceLiveReq> faceLiveReqs = image.stream()
            .map(img -> new FaceLiveReq(img, ImageConstant.IMAGE_URL.getImageStr(), "age,beauty,spoofing", "COMMON"))
            .collect(Collectors.toList());

        return checkLive(key, faceLiveReqs);
    }

    /**
     * 人证审核
     * 
     * @param key
     * @param image
     * @param idCardSide 身份证正反面 front/back
     * @return
     * 用于校验身份证号码、性别、出生是否一致，输出结果及其对应关系如下：
     * -1: 身份证正面所有字段全为空
     * 0: 身份证证号识别错误
     * 1: 身份证证号和性别、出生信息一致
     * 2: 身份证证号和性别、出生信息都不一致
     * 3: 身份证证号和出生信息不一致
     * 4: 身份证证号和性别信息不一致
     * @
     */
    public static IdCardCheckResultDTO checkIdCard(String key, String image, Integer imageType, String idCardSide) {
        log.info("checkIdCard start");
        Map<?, ?> map = getParam(image, imageType, idCardSide);
        HttpResponse httpResponse =
            HttpUtils.doPost(BaiduApiConstant.HOST, BaiduApiConstant.ID_OCR,
                ImmutableMap.of("Content-Type", HttpUtilsConstant.X_WWW_FORM_URLENCODED),
                ImmutableMap.of("access_token", key),
                HttpUtils.urlEncode(map));
        String s = HttpUtils.checkResponseAndGetResult(httpResponse, true);
        IdCardCheckResultDTO idCardCheckResultDTO = JSON.parseObject(s, IdCardCheckResultDTO.class);
        log.info("checkIdCard success idCardCheckResultDTO={}", JSON.toJSONString(idCardCheckResultDTO));
        return idCardCheckResultDTO;
    }

    public static Map<?, ?> getParam(String image, Integer imageType, String idCardSide) {
        return ImmutableMap.<String, Object>builder().put("id_card_side", idCardSide)
            .put("detect_risk", true)
            .put("detect_photo", true).put(ImageConstant.getTypeStr(imageType), image).build();
    }

    public static IdCardCheckResultDTO checkIdCardWithBase64Front(String key, String image) {
        return checkIdCard(key, image, ImageConstant.IMAGE.getImageType(), "front");
    }

    public static IdCardCheckResultDTO checkIdCardWithBase64Back(String key, String image) {
        return checkIdCard(key, image, ImageConstant.IMAGE.getImageType(), "back");
    }

    public static IdCardCheckResultDTO checkIdCardWithUrlFront(String key, String image) {
        return checkIdCard(key, image, ImageConstant.URL.getImageType(), "front");
    }

    public static IdCardCheckResultDTO checkIdCardWithUrlBack(String key, String image) {
        return checkIdCard(key, image, ImageConstant.URL.getImageType(), "back");
    }

    public static IdCardCheckResultDTO checkIdCardWithFileFront(String key, String image) {
        return checkIdCard(key, Base64Util.encodeBase64(FileTools.read(image)), ImageConstant.IMAGE.getImageType(),
            "front");
    }

    public static IdCardCheckResultDTO checkIdCardWithFIleBack(String key, String image) {
        return checkIdCard(key, Base64Util.encodeBase64(FileTools.read(image)), ImageConstant.IMAGE.getImageType(),
            "back");
    }
}
