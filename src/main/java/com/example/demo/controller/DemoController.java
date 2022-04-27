package com.example.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.net.*;
import java.util.*;

import static org.apache.el.util.MessageFactory.get;

@Controller
public class DemoController {


    //GET, POST등의 method할 때 Get임
    @GetMapping("hello") // "/hello"를 입력하면 method가 호출됨
    public String hello(Model model) {
        model.addAttribute("data", "hello!!");
        return "hello";
    }

    @GetMapping("hello-mvc")
    public String helloMvc(@RequestParam("name") String name, Model model) {
        //parameter를 요청 , 정보를 담아서 view로델 보낼 모델

        model.addAttribute("name", name);// 받은 파라미터를 이용해서 name키의 값으로 지정
        return "hello-template"; //hello-template.html로
    }

    @GetMapping("hello-string")
    @ResponseBody // http의 body부분에 이 데이터를 직접 넣어 주겠다는 의미
    public String helloString(@RequestParam("name") String name) {
        return "hello" + name; //viewResolver가 아니고 data가 그대로 전달됨
    }

    @GetMapping("hello-api")
    @ResponseBody
    public Hello helloApi(@RequestParam("name") String name) {
        Hello hello = new Hello(); //Hello객체 생성

        hello.setName(name); //파라미터로 넘어온 name을 이용하여 데이터를 넣음
        return hello; //객체 반환
    }

    static class Hello { //class내에 class를 정의할 수 있음 HelloController.Hello
        private String name;

        //getter -> getName함수로 데이터를 꺼내 사용
        public String getName() {
            return name;
        }

        //setter -> setName함수로 데이터를 입력
        public void setName(String name) {
            this.name = name;
        }
    }

    @GetMapping("naver")
    @ResponseBody
    public String naverApi(@RequestParam("name") String name) throws IOException {
        Naver naver = new Naver(); //Hello객체 생성
        return naver.search(name);//파라미터로 넘어온 name을 이용하여 데이터를 넣음
    }


    static class Naver {
        public String search(String searchWord) throws IOException {
            String text = null;
            try {
                text = URLEncoder.encode(searchWord, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("검색어 인코딩 실패", e);
            }

            String apiURL = "https://openapi.naver.com/v1/search/blog?query=" + text + "&display=20";
            // json 결과
            // String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("X-Naver-Client-Id", "0mjo6hkJAsuDzP5fU6gS");
            requestHeaders.put("X-Naver-Client-Secret", "LcuKnQsz3r");
            String responseBody = get(apiURL, requestHeaders);
//            System.out.println(responseBody);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            try {
                map = mapper.readValue(responseBody, Map.class);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            ArrayList<Map<String, Object>> mapList = (ArrayList<Map<String, Object>>) map.get("items");
            for (int i = 0; i < mapList.size(); i++) {
                System.out.println(mapList.get(i).get("link"));
                String link = (String) mapList.get(i).get("link");
                String html = getMeta(link);
                mapList.get(i).put("description", html);
            }

//            String html = getHTML("https://blog.naver.com//PostView.naver?blogId=papakang3156&logNo=222692642625&from=search&redirect=Log&widgetTypeCall=true&directAccess=false");
//            String html = getMeta("https://blog.naver.com/papakang3156?Redirect=Log&logNo=222692642625");
//            System.out.println(html);

            Gson gson = new Gson();
            JsonObject json = gson.toJsonTree(map).getAsJsonObject();

            return String.valueOf(json);
        }

        private String get(String apiUrl, Map<String, String> requestHeaders) {
            HttpURLConnection con = connect(apiUrl);
            try {
                con.setRequestMethod("GET");
                for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 정상 호출
                    return readBody(con.getInputStream());
                } else { // 에러 발생
                    return readBody(con.getErrorStream());
                }
            } catch (IOException e) {
                throw new RuntimeException("API 요청과 응답 실패", e);
            } finally {
                con.disconnect();
            }
        }

        private HttpURLConnection connect(String apiUrl) {
            try {
                URL url = new URL(apiUrl);
                return (HttpURLConnection) url.openConnection();
            } catch (MalformedURLException e) {
                throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
            } catch (IOException e) {
                throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
            }
        }

        private static String readBody(InputStream body) {
            InputStreamReader streamReader = new InputStreamReader(body);
            try (BufferedReader lineReader = new BufferedReader(streamReader)) {
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = lineReader.readLine()) != null) {
                    responseBody.append(line);
                }
                return responseBody.toString();
            } catch (IOException e) {
                throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
            }
        }
    }

    public static String getHTML(String urlToRead) {
        String result = "";
        try {
            URL url = new URL(urlToRead);

//            System.out.println("url=[" + url + "]");
//            System.out.println("protocol=[" + url.getProtocol() + "]");
//            System.out.println("host=[" + url.getHost() + "]");
//            System.out.println("content=[" + url.getContent() + "]");

            InputStream is = url.openStream();
            int ch;
            while ((ch = is.read()) != -1) {
//                System.out.print((char) ch);
                result += (char) ch;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getMeta(String url) throws IOException {
        // 네이버 블로그 url로 document가져오기
        Document doc = Jsoup.connect(url).get();

        // iframe 태그에 있는 진짜 블로그 주소 가져오기
        org.jsoup.select.Elements iframes = doc.select("iframe#mainFrame");
        String src = iframes.attr("src");
        //진짜 블로그 주소 document 가져오기
        String url2 = "http://blog.naver.com" + src;
        Document doc2 = Jsoup.connect(url2).get();
//        System.out.println("주소 확인용 : " + url2);
//        System.out.println("doc2 : " + doc2);
        // 블로그에서 원하는 블로그 페이지 가져오기
//        String[] blog_logNo = src.split("&");
//        String[] logNo_split = blog_logNo[1].split("=");
//        String logNo = logNo_split[1];

        // 찾고자 하는 블로그 본문 가져오기
//        String real_blog_addr = "div#post-view" + logNo;
//        String blog_element = String.valueOf(doc2.select(real_blog_addr));
        String blog_element = doc2.text();
        String og_image = doc2.select("meta[property=og:image]").get(0).attr("content");
        return blog_element;
    }
}