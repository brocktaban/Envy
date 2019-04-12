package com.brocktaban.envy.fragments.auth


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info
import org.jetbrains.anko.wtf
import android.widget.ProgressBar
import com.brocktaban.envy.MainActivity
import com.brocktaban.envy.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FacebookAuthProvider
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.intentFor


class Login : Fragment(), AnkoLogger {

    private val RC_SIGN_IN = 202

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private lateinit var mActivity: Auth
    private lateinit var mMain: ConstraintLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var facebookLogin: LoginButton
    private lateinit var mGoogleBtn: MaterialButton
    private lateinit var mFacebookBtn: MaterialButton
    private lateinit var mLoginButton: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_login, container, false)

        mActivity = activity as Auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient  = GoogleSignIn.getClient(mActivity, gso)
        mAuth               = FirebaseAuth.getInstance()
        mActivity           = activity as Auth
        mMain               = v.main
        mProgressBar        = v.progressBar
        callbackManager     = CallbackManager.Factory.create()
        facebookLogin       = LoginButton(context)
        mGoogleBtn          = v.btnGoogle
        mFacebookBtn        = v.btnFacebook
        mLoginButton        =v.btnLogin

        facebookLogin.setReadPermissions("email", "public_profile")
        facebookLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                info("facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                info( "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                info("facebook:onError", error)
            }
        })

        v.dontHaveAccount.setOnClickListener { mActivity.changeFragment(SignUp()) }
        mLoginButton.setOnClickListener { signInWithEmailPassword(v) }
        mGoogleBtn.setOnClickListener { signInWithGoogle() }
        mFacebookBtn.setOnClickListener {
            startLogin()
            facebookLogin.performClick()
        }

        return v
    }

    private fun signInWithEmailPassword(v: View) {
        val email = v.etEmail.text.toString()
        val password = v.etPassword.text.toString()

        val emailLayout = v.ilEmail
        val passwordLayout = v.ilPassword

        emailLayout.isErrorEnabled = false
        passwordLayout.isErrorEnabled = false

        if (mActivity.isNullOrEmpty(email)) {
            emailLayout.isErrorEnabled = true
            emailLayout.error = "Email can't be empty"
            return
        }

        if (mActivity.isNullOrEmpty(password)) {
            passwordLayout.isErrorEnabled = true
            passwordLayout.error = "Password can't be empty"
            return
        }

        if (!mActivity.isEmailValid(email)) {
            emailLayout.isErrorEnabled = true
            emailLayout.error = "Email is not valid"
            return
        }

        if (password.length <= 6) {
            passwordLayout.isErrorEnabled = true
            passwordLayout.error = "Password can't be less than 6 characters"
            return
        }

        startLogin()

        mAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    endLogin()

                    if (!task.isSuccessful) {
                        wtf("Could not sign in", task.exception)
                        mMain.longSnackbar(task.exception?.localizedMessage.toString())
                        return@addOnCompleteListener
                    }

                    successLogin()
                }
    }


    private fun signInWithGoogle() {
        startLogin()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        info("firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity) { task ->

                    endLogin()

                    if (task.isSuccessful) {
                        info( "signInWithCredential:success")
                        successLogin()
                    } else {
                        wtf("signInWithCredential:failure", task.exception)
                        mMain.snackbar("Authentication Failed. ${task.exception?.localizedMessage}")
                    }
                }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        info("handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity) { task ->

                    endLogin()

                    if (task.isSuccessful) {
                        info("signInWithCredential:success")
                        successLogin()
                    } else {
                        wtf("signInWithCredential:failure", task.exception)
                        mMain.longSnackbar("Authentication Failed. ${task.exception?.localizedMessage}")
                    }
                }
    }

    private fun startLogin() {
        mProgressBar.visibility = View.VISIBLE
        mGoogleBtn.isEnabled    = false
        mFacebookBtn.isEnabled  = false
        mLoginButton.isEnabled  = false
    }

    private fun endLogin() {
        mProgressBar.visibility = View.GONE
        mGoogleBtn.isEnabled    = true
        mFacebookBtn.isEnabled  = true
        mLoginButton.isEnabled  = true
    }

    private fun successLogin() {
        startActivity(context?.intentFor<MainActivity>())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                endLogin()
                wtf( "Google sign in failed", e)
            }
        }

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
