package flyfish.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.mapper.Travel_Mapper;
import flyfish.pojo.DTO.*;
import flyfish.service.Travel_Service;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Travel_ServiceImpl implements Travel_Service {
    @Autowired
    private Travel_Mapper travel_mapper;
    @Value("${amap.key}")
    private String amapKey;

    @Value("${amap.secret}")
    private String amapSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int STATIC_MAP_WIDTH = 720;
    private static final int STATIC_MAP_HEIGHT = 520;

    // 匹配 http 或 https 开头的链接，直到遇到空格或文本结束
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s]+");

    /**
     * 提取文本中的第一个链接以及链接前的描述文本。
     * @param rawText 原始文案
     * @return 一个长度为2的数组，[0]为描述文本，[1]为链接；如果未找到链接则[0]为原文本,[1]为null
     */
    public static String[] extract(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return new String[]{rawText, null};
        }
        Matcher matcher = URL_PATTERN.matcher(rawText);
        if (matcher.find()) {
            String url = matcher.group();
            String description = rawText.substring(0, matcher.start()).trim();
            return new String[]{description, url};
        }
        // 找不到链接时，整个文本作为描述
        return new String[]{rawText.trim(), null};
    }


    @Override
    public void addScenery(TravelSceneryDTO travelSceneryDTO) {
        // 新景点追加到当前页面最后，数据库中已有空顺序时也不会触发空指针。
        Integer storedMaxOrder = travel_mapper.queryMaxSceneryOrder(
                travelSceneryDTO.getPageName(),
                travelSceneryDTO.getTarget()
        );
        int maxOrder = storedMaxOrder == null ? 0 : storedMaxOrder;
        travelSceneryDTO.setOrder(maxOrder + 1);
        travel_mapper.addScenery(travelSceneryDTO);
    }

    @Override
    public List<TravelSceneryDTO> queryScenery(String pageName, String target) {
        return travel_mapper.queryScenery(pageName,target);
    }

    @Override
    public void updateScenery(TravelSceneryDTO travelSceneryDTO) {
        travel_mapper.updateScenery(travelSceneryDTO);
    }

    @Override
    public void deleteScenery(String id) {
        travel_mapper.deleteScenery(id);
    }


    @Override
    public void addTravelCommnetFunction(TravelCommentFunctionDTO travelCommentFunctionDTO) {
        travelCommentFunctionDTO.setCreateTime(LocalDateTime.now());
        travelCommentFunctionDTO.setRanklevel(0);
        travel_mapper.addCommentFunction(travelCommentFunctionDTO);
    }

    @Override
    public List<TravelCommentFunctionDTO> queryCommentFunction(String pageName, String target) {
        return travel_mapper.queryCommentFunction(pageName,target);
    }

    @Override
    public void updateCommentFunction(TravelCommentFunctionDTO travelCommentFunctionDTO) {
        travel_mapper.updateCommentFunction(travelCommentFunctionDTO);
    }

    @Override
    public void deleteCommentFunction(String id) {
        travel_mapper.deleteCommentFunction(id);
    }

    @Override
    public void queryPassword(String password, String functionButton) {
        Integer id = travel_mapper.queryPassword(password,functionButton);
        if (id == null) {
            throw new RuntimeException("密码错误或功能按钮不存在");
        }
    }


    @Override
    public void addDeliciousFood(TravelDeliciousFood travelDeliciousFood) {
        travel_mapper.addDelecacy(travelDeliciousFood);
    }

    @Override
    public List<TravelDeliciousFood> queryDeliciousFood(String pageName, String target) {
        return travel_mapper.queryDeliciousFoood(pageName,target);
    }

    @Override
    public void updateDeliciousFood(TravelDeliciousFood travelDeliciousFood) {


        travel_mapper.updateDeliciousFood(travelDeliciousFood);
    }

    @Override
    public void deleteDeliciousFood(String id) {
        travel_mapper.deleteDeleciousFood(id);
    }

    @Override
    public void addRecommend(TravelRecommendDTO travelRecommendDTO) {

        String origionUrlInfo = travelRecommendDTO.getRecommendOriginData();
        String url = extract(origionUrlInfo)[1];
        String description = extract(origionUrlInfo)[0];
        travelRecommendDTO.setRecommendUrl(url);
        travelRecommendDTO.setComment(description);
        travel_mapper.addRecommend(travelRecommendDTO);
    }

    @Override
    public List<TravelRecommendDTO> queryRecommend(String pageName, String target) {
        return travel_mapper.queryRecommend(pageName,target);
    }

    @Override
    public void updateRecommend(TravelRecommendDTO travelRecommendDTO) {
        travel_mapper.updateRecommend(travelRecommendDTO);
    }

    @Override
    public void deleteRecommend(String id) {
        travel_mapper.deleteRecommend(id);
    }

    @Override
    public void addImage(TravelImageRecordDTO travelImageRecordDTO) {
        travel_mapper.addImage(travelImageRecordDTO);
    }

    @Override
    public List<TravelImageRecordDTO> queryImage(String pageName, String target, String type) {
        return travel_mapper.queryImage(pageName,target,type);
    }

    @Override
    public void deleteImage(String id) {
        travel_mapper.deleteImage(id);
    }

    @Override
    public void moveScenery(TravelSceneryDTO travelSceneryDTO) {
        //首先是查询到当前景点的顺序，然后查询到比当前景点小的顺序的景点，然后把两个顺序呼唤
        Integer currentOrder = travelSceneryDTO.getOrder();
        List<TravelSceneryDTO> existingSceneries = travel_mapper.queryScenery(travelSceneryDTO.getPageName(), travelSceneryDTO.getTarget());
        // 1. 找到当前景点对象
        TravelSceneryDTO currentScenery = existingSceneries.stream()
                .filter(s -> s.getOrder().equals(currentOrder))
                .findFirst().orElse(null);

// 2. 找到 order 小于 currentOrder 且最大的那个（上一个）
        TravelSceneryDTO prevScenery = existingSceneries.stream()
                .filter(s -> s.getOrder() < currentOrder)
                .max(Comparator.comparingInt(TravelSceneryDTO::getOrder))
                .orElse(null);

        if (currentScenery == null || prevScenery == null) {
            // 已经是第一个，无法上移，或数据异常
            return;
        }

// 3. 交换 order 值
        Integer tempOrder = currentScenery.getOrder();
        currentScenery.setOrder(prevScenery.getOrder());
        prevScenery.setOrder(tempOrder);

// 4. 批量更新数据库（注意事务）
        travel_mapper.updateOrder(currentScenery.getId(), currentScenery.getOrder());
        travel_mapper.updateOrder(prevScenery.getId(), prevScenery.getOrder());
    }

    //这次是向下移动，也就是和比自己顺序高的互换
    @Override
    public void moveScenerydown(TravelSceneryDTO travelSceneryDTO) {
        // 1. 获取当前景点的顺序值
        Integer currentOrder = travelSceneryDTO.getOrder();
        String pageName = travelSceneryDTO.getPageName();
        String target = travelSceneryDTO.getTarget();

        // 2. 查询同一页面下的所有景点（通常已按 order 升序排列）
        List<TravelSceneryDTO> existingSceneries = travel_mapper.queryScenery(pageName, target);
        if (existingSceneries == null || existingSceneries.isEmpty()) {
            log.warn("景点列表为空，无法下移。pageName={}, target={}", pageName, target);
            return;
        }

        // 3. 找到当前景点对象（建议通过唯一ID匹配，这里暂时用 order）
        TravelSceneryDTO currentScenery = existingSceneries.stream()
                .filter(s -> s.getOrder().equals(currentOrder))
                .findFirst()
                .orElse(null);
        if (currentScenery == null) {
            log.warn("当前景点不存在，order={}, pageName={}, target={}", currentOrder, pageName, target);
            return;
        }

        // 4. 找到 order 大于 currentOrder 且最小的那个（下一个景点）
        TravelSceneryDTO nextScenery = existingSceneries.stream()
                .filter(s -> s.getOrder() > currentOrder)
                .min(Comparator.comparingInt(TravelSceneryDTO::getOrder))
                .orElse(null);
        if (nextScenery == null) {
            log.warn("已经是最后一个景点，无法下移。currentOrder={}, pageName={}, target={}", currentOrder, pageName, target);
            return;
        }

        // 5. 交换 order
        Integer tempOrder = currentScenery.getOrder();
        currentScenery.setOrder(nextScenery.getOrder());
        nextScenery.setOrder(tempOrder);

        // 6. 更新数据库
        travel_mapper.updateOrder(currentScenery.getId(), currentScenery.getOrder());
        travel_mapper.updateOrder(nextScenery.getId(), nextScenery.getOrder());

        log.info("景点下移成功。currentId={}, nextId={}, pageName={}, target={}",
                currentScenery.getId(), nextScenery.getId(), pageName, target);
    }





    @Override
    public AmapRouteResponseDTO getAmapDayRoute(AmapRouteRequestDTO requestDTO) {
        List<AmapRoutePointDTO> points = Optional.ofNullable(requestDTO.getPoints()).orElse(Collections.emptyList())
                .stream()
                .filter(p -> p.getLon() != null && p.getLat() != null)
                .collect(Collectors.toList());

        if (points.size() < 2) {
            return new AmapRouteResponseDTO("", "--", "--", Collections.emptyList());
        }

        JsonNode path = queryDrivingPath(
                points.get(0),
                points.get(points.size() - 1),
                points.size() > 2 ? points.subList(1, points.size() - 1) : Collections.emptyList()
        );

        String distanceText = "--";
        String durationText = "--";
        String polyline = points.stream().map(this::lnglat).collect(Collectors.joining(";"));
        if (path != null) {
            distanceText = meterText(path.path("distance").asText(""));
            String duration = path.path("duration").asText("");
            if (duration == null || duration.isEmpty()) {
                duration = path.path("cost").path("duration").asText("");
            }
            durationText = secondText(duration);
            String parsed = collectPolyline(path);
            if (!parsed.isEmpty()) {
                polyline = parsed;
            }
        }

        List<AmapRouteSegmentDTO> segments = buildRouteSegments(points);

        return new AmapRouteResponseDTO(buildStaticMapUrl(points, polyline), distanceText, durationText, segments);
    }

    @Override
    public List<TravelToiletDTO> searchNearbyToilets(Double lon, Double lat, Integer radius) {
        if (lon == null || lat == null) {
            return Collections.emptyList();
        }

        int safeRadius = radius == null ? 3000 : Math.max(500, Math.min(radius, 10000));
        Map<String, String> params = new LinkedHashMap<>();
        params.put("location", fmt(lon) + "," + fmt(lat));
        params.put("keywords", "厕所");
        params.put("types", "200300");
        params.put("radius", String.valueOf(safeRadius));
        params.put("sortrule", "distance");
        params.put("offset", "20");
        params.put("page", "1");
        params.put("extensions", "base");
        params.put("output", "json");

        String json = restTemplate.getForObject(buildAmapUri("/v3/place/around", params), String.class);
        JsonNode pois = readJson(json).path("pois");
        List<TravelToiletDTO> list = new ArrayList<>();
        if (pois.isArray()) {
            for (JsonNode poi : pois) {
                String location = poi.path("location").asText("");
                String name = poi.path("name").asText("公共厕所");
                String mapUrl = location.isEmpty() ? "" :
                        "https://uri.amap.com/marker?position=" + location + "&name=" + urlEncode(name);
                list.add(new TravelToiletDTO(
                        poi.path("id").asText(""),
                        name,
                        poi.path("address").isTextual() ? poi.path("address").asText("") : "",
                        location,
                        poi.path("distance").asText(""),
                        mapUrl
                ));
            }
        }
        return list;
    }

    @Override
    public void moveSceneryup(Integer id) {
        TravelSceneryDTO currentScenery = travel_mapper.querySceneryById(id);
        if (currentScenery == null) {
            return;
        }
        List<TravelSceneryDTO> existingSceneries =
                travel_mapper.queryScenery(currentScenery.getPageName(), currentScenery.getTarget());
        TravelSceneryDTO prevScenery = existingSceneries.stream()
                .filter(s -> s.getOrder() != null && currentScenery.getOrder() != null && s.getOrder() < currentScenery.getOrder())
                .max(Comparator.comparingInt(TravelSceneryDTO::getOrder))
                .orElse(null);
        if (prevScenery == null) {
            return;
        }
        Integer tempOrder = currentScenery.getOrder();
        travel_mapper.updateOrder(currentScenery.getId(), prevScenery.getOrder());
        travel_mapper.updateOrder(prevScenery.getId(), tempOrder);
    }

    @Override
    public void moveScenerydown(Integer id) {
        TravelSceneryDTO currentScenery = travel_mapper.querySceneryById(id);
        if (currentScenery == null) {
            return;
        }
        List<TravelSceneryDTO> existingSceneries =
                travel_mapper.queryScenery(currentScenery.getPageName(), currentScenery.getTarget());
        TravelSceneryDTO nextScenery = existingSceneries.stream()
                .filter(s -> s.getOrder() != null && currentScenery.getOrder() != null && s.getOrder() > currentScenery.getOrder())
                .min(Comparator.comparingInt(TravelSceneryDTO::getOrder))
                .orElse(null);
        if (nextScenery == null) {
            return;
        }
        Integer tempOrder = currentScenery.getOrder();
        travel_mapper.updateOrder(currentScenery.getId(), nextScenery.getOrder());
        travel_mapper.updateOrder(nextScenery.getId(), tempOrder);
    }



    private URI buildAmapUri(String path, Map<String, String> params) {
        Map<String, String> signed = new TreeMap<>(params);
        signed.put("key", amapKey);
        if (amapSecret != null && !amapSecret.isBlank() && !"—".equals(amapSecret) && !"-".equals(amapSecret)) {
            signed.put("sig", amapSig(signed));
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com" + path);
        signed.forEach(builder::queryParam);
        return builder.build().encode(StandardCharsets.UTF_8).toUri();
    }

    private JsonNode queryDrivingPath(AmapRoutePointDTO origin,
                                      AmapRoutePointDTO destination,
                                      List<AmapRoutePointDTO> waypoints) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("origin", lnglat(origin));
        params.put("destination", lnglat(destination));
        if (waypoints != null && !waypoints.isEmpty()) {
            params.put("waypoints", waypoints.stream()
                    .map(this::lnglat)
                    .collect(Collectors.joining(";")));
        }
        params.put("strategy", "32");
        params.put("show_fields", "cost,polyline");
        params.put("output", "json");

        String routeJson = restTemplate.getForObject(buildAmapUri("/v5/direction/driving", params), String.class);
        JsonNode root = readJson(routeJson);
        return root.path("route").path("paths").isArray() && root.path("route").path("paths").size() > 0
                ? root.path("route").path("paths").get(0)
                : null;
    }

    private List<AmapRouteSegmentDTO> buildRouteSegments(List<AmapRoutePointDTO> points) {
        List<AmapRouteSegmentDTO> segments = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            AmapRoutePointDTO from = points.get(i);
            AmapRoutePointDTO to = points.get(i + 1);
            String distanceText = "";
            String durationText = "";
            try {
                JsonNode path = queryDrivingPath(from, to, Collections.emptyList());
                if (path != null) {
                    distanceText = meterText(path.path("distance").asText(""));
                    String duration = path.path("duration").asText("");
                    if (duration == null || duration.isEmpty()) {
                        duration = path.path("cost").path("duration").asText("");
                    }
                    durationText = secondText(duration);
                }
            } catch (Exception e) {
                log.warn("高德分段路线查询失败 from={}, to={}", from.getName(), to.getName(), e);
            }
            segments.add(new AmapRouteSegmentDTO(from.getName(), to.getName(), distanceText, durationText));
        }
        return segments;
    }

    private String buildStaticMapUrl(List<AmapRoutePointDTO> points, String polyline) {
        StaticMapViewport viewport = buildStaticMapViewport(points);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("location", fmt(viewport.centerLon()) + "," + fmt(viewport.centerLat()));
        params.put("zoom", String.valueOf(viewport.zoom()));
        params.put("size", STATIC_MAP_WIDTH + "*" + STATIC_MAP_HEIGHT);
        params.put("scale", "1");
        params.put("traffic", "0");
        String routeLine = simplifyPolyline(polyline, 90);
        if (routeLine.isEmpty()) {
            routeLine = points.stream().map(this::lnglat).collect(Collectors.joining(";"));
        }
        params.put("paths", "8,0x2563eb,1,,:"
                + routeLine);

        Map<String, String> signed = new TreeMap<>(params);
        signed.put("key", amapKey);
        if (amapSecret != null && !amapSecret.isBlank() && !"—".equals(amapSecret) && !"-".equals(amapSecret)) {
            signed.put("sig", amapSig(signed));
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/staticmap");
        signed.forEach(builder::queryParam);
        return builder.build().encode(StandardCharsets.UTF_8).toUriString();
    }

    private StaticMapViewport buildStaticMapViewport(List<AmapRoutePointDTO> points) {
        double minLon = points.stream().mapToDouble(AmapRoutePointDTO::getLon).min().orElse(points.get(0).getLon());
        double maxLon = points.stream().mapToDouble(AmapRoutePointDTO::getLon).max().orElse(points.get(0).getLon());
        double minLat = points.stream().mapToDouble(AmapRoutePointDTO::getLat).min().orElse(points.get(0).getLat());
        double maxLat = points.stream().mapToDouble(AmapRoutePointDTO::getLat).max().orElse(points.get(0).getLat());

        double centerLon = (minLon + maxLon) / 2.0;
        double centerLat = (minLat + maxLat) / 2.0;
        double xSpan = Math.abs(amapMercatorX(maxLon) - amapMercatorX(minLon));
        double ySpan = Math.abs(amapMercatorY(maxLat) - amapMercatorY(minLat));
        int zoom = 12;
        for (int z = 17; z >= 3; z--) {
            double worldSize = 256.0 * Math.pow(2, z);
            double pixelWidth = xSpan * worldSize;
            double pixelHeight = ySpan * worldSize;
            if (pixelWidth <= STATIC_MAP_WIDTH - 160 && pixelHeight <= STATIC_MAP_HEIGHT - 140) {
                zoom = z;
                break;
            }
        }
        return new StaticMapViewport(centerLon, centerLat, zoom);
    }

    private double amapMercatorX(double lon) {
        return (lon + 180.0) / 360.0;
    }

    private double amapMercatorY(double lat) {
        double safeLat = Math.max(-85.05112878, Math.min(85.05112878, lat));
        double sinLat = Math.sin(Math.toRadians(safeLat));
        return 0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI);
    }

    private record StaticMapViewport(double centerLon, double centerLat, int zoom) {
    }

    private String buildMarkers(List<AmapRoutePointDTO> points) {
        String coords = points.stream().limit(10).map(this::lnglat).collect(Collectors.joining(";"));
        return "mid,0xFF3B30,A:" + coords;
    }

    private String buildLabels(List<AmapRoutePointDTO> points) {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < Math.min(points.size(), 10); i++) {
            AmapRoutePointDTO p = points.get(i);
            String content = (i + 1) + "." + p.getName() + " " + Optional.ofNullable(p.getAlt()).orElse("");
            labels.add(content + ",0,1,13,0xFFFFFF,0x172631:" + lnglat(p));
        }
        return String.join("|", labels);
    }

    private String collectPolyline(JsonNode path) {
        List<String> parts = new ArrayList<>();
        JsonNode steps = path.path("steps");
        if (steps.isArray()) {
            for (JsonNode step : steps) {
                String one = step.path("polyline").asText("");
                if (!one.isEmpty()) {
                    parts.add(one);
                }
            }
        }
        return String.join(";", parts);
    }

    private String simplifyPolyline(String polyline, int maxPoints) {
        List<String> routePoints = Arrays.stream(Optional.ofNullable(polyline).orElse("").split(";"))
                .filter(s -> s.contains(","))
                .collect(Collectors.toList());
        if (routePoints.size() <= maxPoints) {
            return String.join(";", routePoints);
        }
        List<String> kept = new ArrayList<>();
        double step = (routePoints.size() - 1) / (double) (maxPoints - 1);
        for (int i = 0; i < maxPoints; i++) {
            kept.add(routePoints.get((int) Math.round(i * step)));
        }
        return String.join(";", kept);
    }

    private String amapSig(Map<String, String> params) {
        String raw = params.entrySet().stream()
                .filter(e -> !"sig".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&")) + amapSecret;
        return md5(raw);
    }

    private String md5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("解析高德返回失败", e);
        }
    }

    private String lnglat(AmapRoutePointDTO p) {
        return fmt(p.getLon()) + "," + fmt(p.getLat());
    }

    private String fmt(Double v) {
        return String.format(Locale.US, "%.6f", v);
    }

    private String meterText(String meters) {
        try {
            double m = Double.parseDouble(meters);
            return m >= 1000 ? String.format(Locale.US, "%.1fkm", m / 1000.0) : Math.round(m) + "m";
        } catch (Exception e) {
            return "--";
        }
    }

    private String secondText(String seconds) {
        try {
            long s = Long.parseLong(seconds);
            long h = s / 3600;
            long m = (s % 3600) / 60;
            if (h > 0) {
                return h + "小时" + m + "分";
            }
            return m + "分钟";
        } catch (Exception e) {
            return "--";
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
