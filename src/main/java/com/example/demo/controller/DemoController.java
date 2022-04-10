package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.apache.el.util.MessageFactory.get;

@Controller
public class DemoController {


    //GET, POST등의 method할 때 Get임
    @GetMapping("hello") // "/hello"를 입력하면 method가 호출됨
    public String hello(Model model) {
        model.addAttribute("data","hello!!");
        return "hello";
    }

    @GetMapping("hello-mvc")
    public String helloMvc(@RequestParam("name") String name, Model model){
        //parameter를 요청 , 정보를 담아서 view로델 보낼 모델

        model.addAttribute("name",name);// 받은 파라미터를 이용해서 name키의 값으로 지정
        return "hello-template"; //hello-template.html로
    }

    @GetMapping("hello-string")
    @ResponseBody // http의 body부분에 이 데이터를 직접 넣어 주겠다는 의미
    public String helloString(@RequestParam("name") String name){
        return "hello" + name; //viewResolver가 아니고 data가 그대로 전달됨
    }

    @GetMapping("hello-api")
    @ResponseBody
    public Hello helloApi(@RequestParam("name") String name){
        Hello hello = new Hello(); //Hello객체 생성

        hello.setName(name); //파라미터로 넘어온 name을 이용하여 데이터를 넣음
        return hello; //객체 반환
    }

    static class Hello { //class내에 class를 정의할 수 있음 HelloController.Hello
        private String name;

        //getter -> getName함수로 데이터를 꺼내 사용
        public String getName(){
            return name;
        }

        //setter -> setName함수로 데이터를 입력
        public void setName(String name){
            this.name = name;
        }
    }

    @GetMapping("naver")
    @ResponseBody
    public String naverApi(@RequestParam("name") String name){
        Naver naver = new Naver(); //Hello객체 생성
        return naver.search(name);//파라미터로 넘어온 name을 이용하여 데이터를 넣음
    }


    static class Naver {
        public String search(String searchWord) {
            String text = null;
            try {
                text = URLEncoder.encode(searchWord, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("검색어 인코딩 실패", e);
            }

            String apiURL = "https://openapi.naver.com/v1/search/blog?query=" + text;
            // json 결과
            // String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("X-Naver-Client-Id", "0mjo6hkJAsuDzP5fU6gS");
            requestHeaders.put("X-Naver-Client-Secret", "LcuKnQsz3r");
            String responseBody = get(apiURL, requestHeaders);
            System.out.println(responseBody);
            return responseBody;
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


//    @GetMapping("blog")
//    @ResponseBody
//    public String blog(@RequestParam("name") String name){
//        PostURL postURL = new PostURL(); //Hello객체 생성
//        postURL(name);
//        //파라미터로 넘어온 name을 이용하여 데이터를 넣음
//    }
//
//    public class PostURL {
//        public void search(String) {
//            try {
//                String param = "name=" + URLEncoder.encode("미니", "UTF-8");
//
//                URL url = new URL(name);
//                URLConnection conn = url.openConnection();
//
//                conn.setDoOutput(true);
//                conn.setUseCaches(false);
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//
//                DataOutputStream out = null;
//
//                try {
//                    out = new DataOutputStream(conn.getOutputStream());
//                    out.writeBytes(param);
//                    out.flush();
//                } finally {
//                    if (out != null) out.close();
//                }
//
//                InputStream is = conn.getInputStream();
//                Scanner scan = new Scanner(is);
//
//                int line = 1;
//                while (scan.hasNext()) {
//                    String str = scan.nextLine();
//                    System.out.println((line++) + ":" + str);
//                }
//                scan.close();
//
//            } catch (MalformedURLException e) {
//                System.out.println("The URL address is incorrect.");
//                e.printStackTrace();
//            } catch (IOException e) {
//                System.out.println("It can't connect to the web page.");
//                e.printStackTrace();
//            }
//        }
//    }
}
