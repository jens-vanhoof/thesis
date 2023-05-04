package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.android.material.textfield.TextInputEditText;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.animation.ModelAnimation;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.SelectionVisualizer;
import com.google.ar.sceneform.ux.TransformableNode;

import android.animation.ObjectAnimator;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.Console;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;
    private List<Integer> dimensions = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        Button cube = findViewById(R.id.cube);
        Button pyramid = findViewById(R.id.pyramid);
        Button sphere  = findViewById(R.id.sphere);
        Button cylinder = findViewById(R.id.cylinder);
        cube.setOnClickListener(view -> loadModel("cube"));
        pyramid.setOnClickListener(view -> loadModel("pyramid"));
        sphere.setOnClickListener(view -> loadModel("sphere"));
        cylinder.setOnClickListener(view -> buttonClicked(view,"cylinder"));
        loadModels();
    }
    public void buttonClicked(View view,String model) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog, null);
        final TextInputEditText heightField = alertLayout.findViewById(R.id.height);
        final TextInputEditText diameterField = alertLayout.findViewById(R.id.diameter);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Dimensions");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        //alert.setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show());

        alert.setPositiveButton("Done", (dialog, which) -> {
            dimensions.clear();
            int z = Integer.parseInt(heightField.getText().toString());
            int x = Integer.parseInt(diameterField.getText().toString());
            dimensions.add(x);
            dimensions.add(x);
            dimensions.add(z);
            loadModel(model);
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }
    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }
    public void loadModel(String name){
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/"+name+".glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        Node modelNode = new Node();
                        modelNode.setRenderable(model);
                        activity.model = modelNode.getRenderable();
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }
    public void loadModels(){
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/sphere.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;

                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }


                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }
    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null || viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(100f);
        model.getScaleController().setMinScale(1f);
        Vector3 test = new Vector3((float)dimensions.get(0),(float)dimensions.get(2),(float)dimensions.get(1));
        anchorNode.setLocalScale(test);
        model.setParent(anchorNode);
        model.setRenderable(this.model).animate(true).start();

//        model.setRenderable(this.model);
//        ModelAnimator.ofAnimationFrame(model.getRenderableInstance(), "Idle",this.model.getAnimationFrameRate()).start();
//        ObjectAnimator objectAnimator = ModelAnimator.ofAnimation(model.getRenderableInstance(),"Walk");
//        Button btn = findViewById(R.id.stop);
//        btn.setOnClickListener(view -> objectAnimator.start());
        model.select();
        ((Box) model.getRenderable().getCollisionShape()).getSize();

//        Node titleNode = new Node();
//        titleNode.setParent(model);
//        titleNode.setEnabled(false);
//        titleNode.setLocalPosition(new Vector3(0.0f, 1.0f, 0.0f));
//        titleNode.setRenderable(viewRenderable);
//        titleNode.setEnabled(true);
    }
}
