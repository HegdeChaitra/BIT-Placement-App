package com.example.inspiron.bitplacementc;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import com.example.inspiron.bitplacementc.Models.ServerRequest;
import com.example.inspiron.bitplacementc.Models.ServerResponse;
import com.example.inspiron.bitplacementc.Models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private AppCompatButton btn_login;
    private EditText et_email,et_password;
    private TextView tv_register;
    private ProgressBar progress;
    private SharedPreferences pref;



    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_login, container, false);
        initViews(view);
        return view;
    }


    private void initViews(View view){
        pref=getActivity().getPreferences(0);

        btn_login = (AppCompatButton)view.findViewById(R.id.btn_login);
        tv_register = (TextView)view.findViewById(R.id.tv_register);
        et_email = (EditText)view.findViewById(R.id.et_email);
        et_password = (EditText)view.findViewById(R.id.et_password);

        progress = (ProgressBar)view.findViewById(R.id.progress);

        btn_login.setOnClickListener(this);
        tv_register.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tv_register:
                goToRegister();
                break;

            case R.id.btn_login:
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if(!email.isEmpty() && !password.isEmpty()) {

                    progress.setVisibility(View.VISIBLE);
                    loginProcess(email,password);

                } else {

                    Snackbar.make(getView(), "Fields are empty !", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

private void loginProcess(String email, String paswd){

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
            RequestInterface requestInterface = retrofit.create(RequestInterface.class);

            User user = new User();
            user.setEmail(email);
            user.setPassword(paswd);
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.LOGIN_OPERATION);
            request.setUser(user);
            Call<ServerResponse> response = requestInterface.operation(request);

    response.enqueue(new Callback<ServerResponse>() {
        @Override
        public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

            ServerResponse resp = response.body();
            Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();

            if(resp.getResult().equals(Constants.SUCCESS)){
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(Constants.IS_LOGGED_IN,true);
                editor.putString(Constants.EMAIL,resp.getUser().getEmail());
                editor.putString(Constants.NAME,resp.getUser().getName());
                editor.putString(Constants.UNIQUE_ID,resp.getUser().getUnique_id());
                editor.apply();
                Snackbar.make(getView(), "Logged in", Snackbar.LENGTH_LONG).show();
                goToProfile();

            }
            progress.setVisibility(View.INVISIBLE);

        }


        @Override
        public void onFailure(Call<ServerResponse> call, Throwable t) {

            progress.setVisibility(View.INVISIBLE);
            Log.d(Constants.TAG,"failed");
            Snackbar.make(getView(), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            goToProfile();

        }
    });

}


    private void goToProfile() {
        startActivity(new Intent(LoginFragment.this.getActivity(), Home.class));
    }


    private void goToRegister(){

        Fragment profile=new RegisterFragment();
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,profile);
        ft.commit();

    }

}
