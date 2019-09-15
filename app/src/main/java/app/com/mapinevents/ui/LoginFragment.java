package app.com.mapinevents.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Objects;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.LoginFragmentBinding;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.LoginViewModel;

import static app.com.mapinevents.ui.MainActivity.RC_SIGN_IN;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    private LoginFragmentBinding binding;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private View rootView;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_android_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), googleSignInOptions);

        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Handle success
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                        ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                        binding.googleSignInButton.setEnabled(true);
                        binding.facebookSignInButton.setEnabled(true);
                        Snackbar.make(binding.getRoot(), "Dialog Cancelled", Snackbar.LENGTH_LONG).show();

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        int disappointedFace = 0x1F61E;

                        ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                        binding.googleSignInButton.setEnabled(true);
                        binding.facebookSignInButton.setEnabled(true);

                        Snackbar.make(binding.getRoot(), "Error " + Utils.getEmojiByUnicode(disappointedFace) +
                                "Please try again later.", Snackbar.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LoginFragmentBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        // TODO: Use the ViewModel
        binding.googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.facebookSignInButton.setEnabled(false);
                view.setEnabled(false);
                ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar();
                signIn();
            }
        });

        binding.facebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.facebookSignInButton == v) {
                    binding.googleSignInButton.setEnabled(false);
                    v.setEnabled(false);
                    LoginManager.getInstance().logInWithReadPermissions(
                            LoginFragment.this,
                            Arrays.asList("email",
                                    "public_profile")
                    );

                    ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar();
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace();

                ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                binding.googleSignInButton.setEnabled(true);
                binding.facebookSignInButton.setEnabled(true);

                Snackbar.make(binding.getRoot(), "Unable to login", Snackbar.LENGTH_LONG).show();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                            binding.googleSignInButton.setEnabled(true);
                            binding.facebookSignInButton.setEnabled(true);
                            Navigation.findNavController(rootView).navigate(R.id.homeFragment);

                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(binding.getRoot(), "Unable to login", Snackbar.LENGTH_SHORT)
                                    .show();

                            binding.googleSignInButton.setEnabled(true);
                            binding.facebookSignInButton.setEnabled(true);
                            ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Snackbar.make(binding.getRoot(), "Unable to login with Facebook", Snackbar.LENGTH_SHORT).show();
                        ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                        binding.googleSignInButton.setEnabled(true);
                        binding.facebookSignInButton.setEnabled(true);
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            binding.googleSignInButton.setEnabled(true);
                            binding.facebookSignInButton.setEnabled(true);
                            Navigation.findNavController(rootView).navigate(R.id.homeFragment);
                            ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                        } else {
                            Snackbar.make(rootView, "Sign in with Facebook failed" + Objects.requireNonNull(task.getException()).getMessage(), Snackbar.LENGTH_SHORT)
                                    .show();
                            binding.googleSignInButton.setEnabled(true);
                            binding.facebookSignInButton.setEnabled(true);
                            ((MainActivity) Objects.requireNonNull(getActivity())).hideProgressBar();
                        }

                        // ...
                    }
                });
    }

}
