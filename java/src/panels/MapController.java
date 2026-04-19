package panels;

import java.io.File;
import java.io.FileWriter;
import java.awt.Desktop;
import java.sql.*;

public class MapController {

    public static void openFullMap() {
        try {
            File tempFile = File.createTempFile("relief_map", ".html");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(generateHtml());
            }
            Desktop.getDesktop().browse(tempFile.toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateHtml() {
        StringBuilder markersJs    = new StringBuilder();
        StringBuilder heatPointsJs = new StringBuilder("var heatPoints = [];\n");
        StringBuilder dbErrors     = new StringBuilder();
        int markerCount = 0;

        // ── Always-visible hardcoded test markers (prove rendering works) ──
        // These show even if DB is empty / connection fails
        String[][] testShelters = {
            {"Puri Relief Camp",           "19.8135", "85.8312", "S"},
            {"Berhampur Safehouse",        "19.3150", "84.7941", "S"},
            {"Bhubaneswar Central",        "20.2961", "85.8245", "S"},
            {"Balasore Storm Center",      "21.4942", "86.9317", "S"},
            {"Vizag Cyclone Shelter",      "17.6868", "83.2185", "S"},
            {"Chennai Coastal Safehouse",  "13.0475", "80.2824", "S"},
        };
        String[][] testVictims = {
            {"Puri Cluster (CRITICAL)",    "19.8220", "85.8350", "C"},
            {"Vizag Cluster (CRITICAL)",   "17.7020", "83.2520", "C"},
            {"Chennai Cluster (CRITICAL)", "13.0490", "80.2840", "C"},
        };

        try (Connection conn = DBConnection.getConnection()) {

            // ── 1. Shelters from DB ───────────────────────────────────
            boolean shelterDbOk = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM Shelters WHERE latitude IS NOT NULL AND longitude IS NOT NULL")) {
                while (rs.next()) {
                    shelterDbOk = true;
                    String name = rs.getString("name").replace("'", "\\'");
                    int occ = rs.getInt("occupancy");
                    int cap = rs.getInt("capacity");
                    String status = occ >= cap ? "FULL" : occ >= cap * 0.8 ? "NEAR FULL" : "AVAILABLE";
                    markerCount++;
                    markersJs.append(String.format(
                        "addPin(%f,%f,'#1565C0','S','<b>🏥 %s</b><br>%d/%d | %s');\n",
                        rs.getDouble("latitude"), rs.getDouble("longitude"),
                        name, occ, cap, status));
                }
            }
            if (!shelterDbOk) dbErrors.append("⚠️ Shelters table has no lat/lon data. ");

            // ── 2. Victim heat zones from DB ──────────────────────────
            boolean victimDbOk = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM view_map_master_heat WHERE latitude IS NOT NULL AND longitude IS NOT NULL")) {
                while (rs.next()) {
                    victimDbOk = true;
                    double lat = rs.getDouble("latitude");
                    double lon = rs.getDouble("longitude");
                    int sev = rs.getInt("intensity");
                    boolean pulse = rs.getBoolean("pulse_flag");
                    String label = rs.getString("label").replace("'", "\\'");
                    String detail = rs.getString("detail").replace("'", "\\'");

                    double intensity = Math.min(1.0, (sev / 5.0) * 1.5);
                    heatPointsJs.append(String.format("heatPoints.push([%f,%f,%.2f]);\n", lat, lon, intensity));

                    String color = sev >= 5 ? "#C62828" : sev >= 3 ? "#E65100" : "#F9A825";
                    String lbl   = sev >= 5 ? "C5" : sev >= 3 ? "U" + sev : "S" + sev;
                    String popup = String.format("<b>%s</b><br>%s<br>Severity: <b>%d/5</b>%s",
                        label, detail, sev, pulse ? "<br>⚠️ <b>ISOLATED</b>" : "");
                    markerCount++;
                    markersJs.append(String.format("addPin(%f,%f,'%s','%s','%s');\n",
                        lat, lon, color, lbl, popup));
                }
            }
            if (!victimDbOk) dbErrors.append("⚠️ view_map_master_heat returned no rows. ");

        } catch (SQLException e) {
            e.printStackTrace();
            dbErrors.append("❌ DB Connection failed: ").append(e.getMessage().replace("'","").replace("\n","").substring(0, Math.min(80, e.getMessage().length())));
        }

        // If DB gave us nothing, fall back to hardcoded data so map is never blank
        boolean useHardcoded = markerCount == 0;
        if (useHardcoded) {
            for (String[] s : testShelters) {
                markersJs.append(String.format("addPin(%s,%s,'#1565C0','%s','<b>🏥 %s</b> [SAMPLE]');\n",
                    s[1], s[2], s[3], s[0]));
            }
            for (String[] v : testVictims) {
                double lat = Double.parseDouble(v[1]);
                double lon = Double.parseDouble(v[2]);
                heatPointsJs.append(String.format("heatPoints.push([%f,%f,1.0]);\n", lat, lon));
                markersJs.append(String.format("addPin(%s,%s,'#C62828','%s','<b>%s</b> [SAMPLE]');\n",
                    v[1], v[2], v[3], v[0]));
            }
            markerCount = testShelters.length + testVictims.length;
            dbErrors.append(" → Showing sample data.");
        }

        String dbMsg = dbErrors.length() > 0
            ? "<div id='dbwarn'>" + dbErrors.toString() + "</div>"
            : "";

        return "<!DOCTYPE html>\n" +
               "<html><head>\n" +
               "  <meta charset='UTF-8'/>\n" +
               "  <title>Relief-OP Command Center</title>\n" +
               "  <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>\n" +
               "  <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
               "  <script src='https://unpkg.com/leaflet.heat/dist/leaflet-heat.js'></script>\n" +
               "  <style>\n" +
               "    html,body{height:100%;margin:0;background:#0d1117;font-family:'Segoe UI',sans-serif;}\n" +
               "    #map{height:100vh;width:100%;}\n" +
               "    .info-bar{\n" +
               "      position:fixed;top:0;left:0;right:0;\n" +
               "      background:rgba(10,15,25,0.92);\n" +
               "      color:#fff;text-align:center;\n" +
               "      padding:8px;font-size:13px;z-index:9999;\n" +
               "      letter-spacing:1px;border-bottom:2px solid #007aff;\n" +
               "    }\n" +
               "    #dbwarn{\n" +
               "      position:fixed;top:40px;left:50%;transform:translateX(-50%);\n" +
               "      background:#7B1FA2;color:#fff;padding:6px 16px;\n" +
               "      border-radius:8px;font-size:12px;z-index:9999;\n" +
               "      max-width:80%;text-align:center;\n" +
               "    }\n" +
               "    .legend{\n" +
               "      background:rgba(15,20,30,0.93);padding:12px 16px;\n" +
               "      border-radius:10px;color:#eee;\n" +
               "      border:1px solid #007aff;line-height:1.9;\n" +
               "      box-shadow:0 4px 20px rgba(0,0,0,0.5);\n" +
               "      font-size:12px;\n" +
               "    }\n" +
               "  </style>\n" +
               "</head><body>\n" +
               "  <div class='info-bar'>⚡ RELIEF-OP COMMAND CENTER &nbsp;|&nbsp; " + markerCount + " MARKERS &nbsp;|&nbsp; " + (useHardcoded ? "⚠️ SAMPLE DATA (DB empty)" : "✅ LIVE DATABASE") + "</div>\n" +
               dbMsg + "\n" +
               "  <div id='map' style='margin-top:36px;height:calc(100vh - 36px)'></div>\n" +
               "  <script>\n" +
               "    var map = L.map('map').setView([19.82,85.83],7);\n" +
               "    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',\n" +
               "      {maxZoom:19,attribution:'© CartoDB | Relief-OP',subdomains:'abcd'}).addTo(map);\n" +
               "\n" +
               "    // ── addPin helper: encoded SVG → L.icon ──\n" +
               "    function addPin(lat, lng, color, label, popupHtml) {\n" +
               "      var svg = '<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"30\" height=\"42\" viewBox=\"0 0 30 42\">'\n" +
               "        + '<path d=\"M15 0C6.7 0 0 6.7 0 15c0 10 15 27 15 27S30 25 30 15C30 6.7 23.3 0 15 0z\"'\n" +
               "        + ' fill=\"' + color + '\" stroke=\"white\" stroke-width=\"2\"/>'\n" +
               "        + '<text x=\"15\" y=\"19\" text-anchor=\"middle\" dominant-baseline=\"middle\"'\n" +
               "        + ' font-size=\"10\" font-family=\"Arial,sans-serif\" fill=\"white\" font-weight=\"bold\">'\n" +
               "        + label + '</text>'\n" +
               "        + '</svg>';\n" +
               "      var url = 'data:image/svg+xml;base64,' + btoa(svg);\n" +
               "      var icon = L.icon({iconUrl:url,iconSize:[30,42],iconAnchor:[15,42],popupAnchor:[0,-44]});\n" +
               "      L.marker([lat,lng],{icon:icon}).addTo(map).bindPopup(popupHtml);\n" +
               "    }\n" +
               "\n" +
               "    // ── Markers ──\n" +
               "    " + markersJs.toString() + "\n" +
               "\n" +
               "    // ── Heat layer ──\n" +
               "    " + heatPointsJs.toString() + "\n" +
               "    var heat = L.heatLayer(heatPoints,{\n" +
               "      radius:45,blur:25,max:1.0,\n" +
               "      gradient:{0.4:'blue',0.6:'cyan',0.8:'yellow',1.0:'red'}\n" +
               "    }).addTo(map);\n" +
               "    L.control.layers(null,{'🔥 Heat Layer':heat},{position:'topright'}).addTo(map);\n" +
               "\n" +
               "    // ── Legend ──\n" +
               "    var legend=L.control({position:'bottomleft'});\n" +
               "    legend.onAdd=function(){\n" +
               "      var d=L.DomUtil.create('div','legend');\n" +
               "      d.innerHTML='<b style=\"color:#007aff\">⚡ MARKER LEGEND</b><br>'\n" +
               "        +'<span style=\"color:#C62828\">●</span> <b>C5</b> = Critical (Severity 5)<br>'\n" +
               "        +'<span style=\"color:#E65100\">●</span> <b>U</b> = Urgent (3-4)<br>'\n" +
               "        +'<span style=\"color:#F9A825\">●</span> <b>S</b> = Stable (1-2)<br>'\n" +
               "        +'<span style=\"color:#1565C0\">●</span> <b>S</b> = Shelter / Hub';\n" +
               "      return d;\n" +
               "    };\n" +
               "    legend.addTo(map);\n" +
               "\n" +
               "    // ── Auto-fit ──\n" +
               "    setTimeout(function(){\n" +
               "      map.invalidateSize();\n" +
               "      var grp=new L.featureGroup();\n" +
               "      map.eachLayer(function(l){if(l instanceof L.Marker)grp.addLayer(l);});\n" +
               "      if(grp.getLayers().length>0)map.fitBounds(grp.getBounds().pad(0.15));\n" +
               "    },500);\n" +
               "  </script>\n" +
               "</body></html>";
    }
}