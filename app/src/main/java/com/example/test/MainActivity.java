package com.example.test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.android.filament.Box;
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
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    private ArFragment arFragment;
    private Renderable model;
    private List<Integer> dimensions = new ArrayList();
    private boolean height = false;
    private boolean width = false;
    private boolean diameter = false;

    private boolean length = false;
    private String source;


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

        //Button cube = findViewById(R.id.cube);
        Button pyramid = findViewById(R.id.pyramid);
        Button sphere  = findViewById(R.id.sphere);
        Button cylinder = findViewById(R.id.cylinder);
        //cube.setOnClickListener(view -> loadModel("cube"));
        pyramid.setOnClickListener(view -> pyramidDialog("pyramid"));
        sphere.setOnClickListener(view -> sphereDialog("sphere"));
        cylinder.setOnClickListener(view -> cylinderDialog("cylinder"));
        loadModels();
    }

    public void cylinderDialog( String model) {
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
            diameter = true;
            height = true;
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }
    public void pyramidDialog(String model) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_pyramid, null);
        final TextInputEditText heightField = alertLayout.findViewById(R.id.height);
        final TextInputEditText widthField = alertLayout.findViewById(R.id.width);
        final TextInputEditText lengthField = alertLayout.findViewById(R.id.length);

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
            int x = Integer.parseInt(widthField.getText().toString());
            int y = Integer.parseInt(lengthField.getText().toString());
            dimensions.add(x);
            dimensions.add(y);
            dimensions.add(z);
            loadModel(model);
            height = true;
            width = true;
            length = true;
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }
    public void sphereDialog(String model) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_sphere, null);
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
            int x = Integer.parseInt(diameterField.getText().toString());
            dimensions.add(x);
            dimensions.add(x);
            dimensions.add(x);
            loadModel(model);
            diameter = true;
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
    private GestureDetector gestureDetector;
    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    public void loadModel(String name){
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        height = false;
        diameter = false;
        width = false;
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/"+name+".glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        Node modelNode = new Node();
                        modelNode.setName("kaka");
                        modelNode.setRenderable(model);
                        source = name;
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

    }
    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }
    private Node findNodeByName(List<Node> children,String name){
        for (Node n : children) {
            if(n.getName() == name) return n;
        }
        return null;
    }
    private void updateDimensions(Node modelNode){
            Node node = arFragment.getTransformationSystem().getSelectedNode();
            if(node != null && node.getChildren() != null){
                Vector3 vector = node.getChildren().get(0).getWorldScale();
                updateTextFields(this.diameter, node, "diameter", vector.x);
                updateTextFields(this.height, node, "height", vector.y);
                updateTextFields(this.length, node, "length", vector.z);
                updateTextFields(this.width, node, "width", vector.x);
                double volumeValue = getVolumeValue(node.getName());

                TextView v = (TextView) findViewById(R.id.volume);
                v.setText(String.format("Volume=%.2fcm\u00B3", volumeValue));
            }
    }

    private void updateTextFields(boolean prop, Node node, String name, float vector) {
        if(prop){
            Node n = findNodeByName(node.getChildren(), name);
            ViewRenderable view = (ViewRenderable) n.getRenderable();
            if(view != null) ((TextView) view.getView().findViewById(R.id.test)).setText(String.format("%.2f", vector));
        }
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        if (model == null) {
            Toast.makeText(this, "no model present", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        Node modelNode = new Node();
        model.setName(source);
        modelNode.setParent(model);
        RenderableInstance instance = modelNode.setRenderable(this.model);
        Box boundingBox = instance.getFilamentAsset().getBoundingBox();
        model.getScaleController().setMaxScale(100f);
        model.getScaleController().setMinScale(1f);
        Vector3 test = new Vector3((float)dimensions.get(0),(float)dimensions.get(2),(float)dimensions.get(1));
        if (boundingBox != null) {
            modelNode.setLocalScale(test);
        }
        //anchorNode.setLocalScale(test);
        model.setParent(anchorNode);
        //model.setRenderable(this.model).animate(true).start();

//        model.setRenderable(this.model);
//        ModelAnimator.ofAnimationFrame(model.getRenderableInstance(), "Idle",this.model.getAnimationFrameRate()).start();
//        ObjectAnimator objectAnimator = ModelAnimator.ofAnimation(model.getRenderableInstance(),"Walk");
//        Button btn = findViewById(R.id.stop);
//        btn.setOnClickListener(view -> objectAnimator.start());
        model.select();

        Vector3 diameterPosition = new Vector3(0.0f, (float)dimensions.get(2)/100+0.04f, 0.0f);
        Node diameter = addNode(model,diameterPosition,"diameter",dimensions.get(0));

        Vector3 heightPosition = new Vector3((float)dimensions.get(0)/100, ((float)dimensions.get(2)/100)/2-0.04f, 0.0f);
        Node height = addNode(model,heightPosition,"height",dimensions.get(2));

        Vector3 lengthPosition = new Vector3((float)dimensions.get(0)/100, -0.08f, 0.0f);
        Node length = addNode(model,lengthPosition,"length",dimensions.get(0),Quaternion.axisAngle(new Vector3(0f,1f,0f),90));

        Vector3 widthPosition = new Vector3(0.0f, -0.08f, (float)dimensions.get(0)/100);
        Node width = addNode(model,widthPosition,"width",dimensions.get(1));

        double volumeValue = getVolumeValue(model.getName());

        TextView v = (TextView) findViewById(R.id.volume);
        v.setText(String.format("Volume=%.2fcm\u00B3", volumeValue));
        arFragment.getArSceneView().getScene().addOnUpdateListener(view -> updateDimensions(model));


        if(this.height){
            height.setEnabled(true);
        }
        if(this.diameter){
            diameter.setEnabled(true);
        }
        if(this.length){
            height.setLocalPosition(new Vector3(0.0f, (float)dimensions.get(2)/100+0.04f, 0.0f));
            length.setEnabled(true);
        }
        if(this.width){
            width.setEnabled(true);
        }

    }

    private double getVolumeValue(String model) {
        if(model == "cylinder"){
            double d = dimensions.get(0).doubleValue();
            double h = dimensions.get(2).doubleValue();
            return Math.PI * Math.pow(d/2,2) * h;
        }else if(model == "sphere"){
            double r = dimensions.get(0).doubleValue()/2;
            return (4/3)*Math.PI*Math.pow(r,3);
        }
        if(model == "pyramid"){
            double l = dimensions.get(0).doubleValue();
            double w = dimensions.get(1).doubleValue();
            double h = dimensions.get(2).doubleValue();
            return (l*w*h)/3;
        }
        return 0;
    }

    @Nullable
    private Node addNode(TransformableNode model, Vector3 position, String name, float content) {
        Node node = new Node();
        node.setParent(model);
        node.setEnabled(false);
        node.setLocalPosition(position);
        node.setLocalScale(new Vector3(0.4f,0.4f,0.4f));
        node.setName(name);
        ViewRenderable.builder()
                        .setView(this, R.layout.view_model_title)
                        .build()
                        .thenAccept(renderable -> {
                                ((TextView) renderable.getView().findViewById(R.id.test)).setText(Float.toString(content));
                            node.setRenderable(renderable);
                        }).exceptionally(throwable -> {
                            Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                            return null;
                        });
        return node;
    }

    @Nullable
    private Node addNode(TransformableNode model, Vector3 position, String name, float content, Quaternion quaternion ) {
        Node node = new Node();
        node.setParent(model);
        node.setEnabled(false);
        node.setLocalPosition(position);
        node.setLocalRotation(quaternion);
        node.setLocalScale(new Vector3(0.4f,0.4f,0.4f));
        node.setName(name);
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(renderable -> {
                    ((TextView) renderable.getView().findViewById(R.id.test)).setText(Float.toString(content));
                    node.setRenderable(renderable);
                }).exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        return node;
    }
}
