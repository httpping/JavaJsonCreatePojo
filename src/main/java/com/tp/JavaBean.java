package com.tp;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;

public class JavaBean {
    public long ll;
    public List<String> images = new ArrayList<>();
    public List<Integer> price = new ArrayList<>();
    public CatBean cat;
    public List<DogBean> dog = new ArrayList<>();
    public JavaBean(String json) {
        this(new JSONObject(json));
    }
    public JavaBean(JSONObject json) {
        ll = json.optLong("ll");
        JSONArray imagesArray =  json.optJSONArray("images") ;
        if (imagesArray != null && imagesArray.length() > 0) {
            for (int i = 0; i < imagesArray.length(); i++) {
                images.add((String)imagesArray.get(i));
            }
        }

        JSONArray priceArray =  json.optJSONArray("price") ;
        if (priceArray != null && priceArray.length() > 0) {
            for (int i = 0; i < priceArray.length(); i++) {
                price.add((int)priceArray.get(i));
            }
        }

        cat = new CatBean(json.optJSONObject("cat"));
        JSONArray dogArray =  json.optJSONArray("dog") ;
        if (dogArray != null && dogArray.length() > 0) {
            for (int i = 0; i < dogArray.length(); i++) {
                dog.add(new DogBean((JSONObject)dogArray.get(i)));
            }
        }


    }

    public static class CatBean {
        public String name;
        public CatBean(String json) {
            this(new JSONObject(json));
        }
        public CatBean(JSONObject json) {
            name = json.optString("name");

        }
    }

    public static class DogBean {
        public String name;
        public int count;
        public boolean twoFeet;
        public String breed;
        public DogBean(String json) {
            this(new JSONObject(json));
        }
        public DogBean(JSONObject json) {
            name = json.optString("name");
            count = json.optInt("count");
            twoFeet = json.optBoolean("twoFeet");
            breed = json.optString("breed");

        }
    }
}
