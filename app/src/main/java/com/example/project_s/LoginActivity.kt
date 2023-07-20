package com.example.project_s

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sign
var loginMode:Boolean = true
class LoginActivity : AppCompatActivity() {
    lateinit var emailEt: EditText
    lateinit var passwordEt: EditText
    lateinit var loginBtn: Button
    lateinit var signupTv: TextView
    lateinit var auth: FirebaseAuth
    lateinit var arirangImg:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEt = findViewById(R.id.email_et)
        passwordEt = findViewById(R.id.pwd_et)
        loginBtn = findViewById(R.id.loginBtn)
        signupTv = findViewById(R.id.signupTv)
        arirangImg = findViewById(R.id.arirangImg)
        requestPermission()
        arirangImg.setOnClickListener {
            if(loginMode){
                arirangImg.setImageResource(R.drawable.arirang_grey)
                loginMode = !loginMode
            }
            else{
                arirangImg.setImageResource(R.drawable.arirang)
                loginMode = !loginMode
            }
        }
        //입력창이 비어있을 시 버튼 비활성화
        loginBtn.isEnabled = false
        emailEt.addTextChangedListener {
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()
            var enabled = email.isNotEmpty() && password.isNotEmpty()
            loginBtn.isEnabled = enabled
        }
        passwordEt.addTextChangedListener {
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()
            var enabled = email.isNotEmpty() && password.isNotEmpty()
            loginBtn.isEnabled = enabled
        }
        //로그인 구현
        loginBtn.setOnClickListener {
            //로딩
            showLoadingDialog()
            var email = emailEt.text.toString()
            var password = passwordEt.text.toString()
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this,"로그인에 성공했습니다!",Toast.LENGTH_SHORT).show()
                        if(loginMode){
                            var intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        else{
                            var intentMode = Intent(this, ModeMainActivity::class.java)
                            startActivity(intentMode)
                        }

                    }
                    else{
                        Toast.makeText(this,"아이디와 비밀번호를 확인해주세요.",Toast.LENGTH_SHORT).show()
                    }
                }
        }
        //회원가입 구현
        signupTv.setOnClickListener {
            if(!loginBtn.isEnabled){
                Toast.makeText(this,"이메일과 비밀번호를 입력해주세요.",Toast.LENGTH_SHORT).show()
            }
            else{
                //로딩
                showLoadingDialog()
                var email = emailEt.text.toString()
                var password = passwordEt.text.toString()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            Toast.makeText(this,"회원가입에 성공했습니다!",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this,"이미 존재하는 계정이거나, 회원가입에 실패했습니다.",Toast.LENGTH_SHORT).show()
                        }
                    }
            }

        }

    }
    //로딩 다이얼로그를 2초 동안 띄워주는 함수
    private fun showLoadingDialog(){
        val dialog = LoadingDialog(this@LoginActivity)
        CoroutineScope(Main).launch {
            dialog.show()
            delay(2000)
            dialog.dismiss()
        }
    }
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 거부해도 계속 노출됨. ("다시 묻지 않음" 체크 시 노출 안됨.)
            // 허용은 한 번만 승인되면 그 다음부터 자동으로 허용됨.
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }
}