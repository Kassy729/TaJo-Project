package com.example.resultmap;

import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.AuthFailureError;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {

    final static private String URL = "https://tazoapp.site/auth/login";
    private Map<String, String> map;



    public LoginRequest(String email, String password, Response.Listener<String>listener ,Response.ErrorListener err){
        super(Method.POST, URL, listener, err);

        map = new HashMap<>();
        map.put("email", email);
        map.put("password", password);
    }



    @Override
    protected Map<String, String>getParams() throws AuthFailureError{
//        System.out.println("오류3");
        return map;
    }
}
