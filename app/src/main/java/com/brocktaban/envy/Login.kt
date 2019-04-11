package com.brocktaban.envy


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
import android.app.ProgressDialog
import android.widget.ProgressBar


class Login : Fragment(), AnkoLogger {

    private val RC_SIGN_IN = 202

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var mActivity: Auth
    private lateinit var mMain: ConstraintLayout
    private lateinit var mProgressBar: ProgressBar


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_login, container, false)

        mActivity = activity as Auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient  = GoogleSignIn.getClient(mActivity, gso)
        auth                = FirebaseAuth.getInstance()
        mActivity           = activity as Auth
        mMain               = v.main
        mProgressBar        = v.progressBar

        v.btnLogin.setOnClickListener { mActivity.changeFragment(SignUp()) }
        v.btnGoogle.setOnClickListener { signInWithGoogle() }

        return v
    }


    private fun signInWithGoogle() {
        mProgressBar.visibility = View.VISIBLE
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        info("firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity) { task ->

                    mProgressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        info( "signInWithCredential:success")
                        val user = auth.currentUser

                    } else {
                        wtf("signInWithCredential:failure", task.exception)
                        mMain.snackbar("Authentication Failed")
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                mProgressBar.visibility = View.GONE
                wtf( "Google sign in failed", e)
            }
        }
    }
}
