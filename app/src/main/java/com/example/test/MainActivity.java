package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.android.filament.Box;
import com.google.android.material.textfield.TextInputEditText;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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
                if(this.diameter){
                    Node diameter = findNodeByName(node.getChildren(),"diameter");
                    ViewRenderable diameterView = (ViewRenderable) diameter.getRenderable();
                    if(diameterView != null) ((TextView) diameterView.getView().findViewById(R.id.test)).setText(String.format("%.2f", vector.x));
                }
                if(this.height){
                    Node height = findNodeByName(node.getChildren(),"height");
                    ViewRenderable heightView = (ViewRenderable) height.getRenderable();
                    if(heightView != null) ((TextView) heightView.getView().findViewById(R.id.test)).setText(String.format("%.2f", vector.y));
                }
                if(this.length){
                    Node length = findNodeByName(node.getChildren(),"length");
                    ViewRenderable lengthView = (ViewRenderable) length.getRenderable();
                    if(lengthView != null) ((TextView) lengthView.getView().findViewById(R.id.test)).setText(String.format("%.2f", vector.z));
                }
                if(this.width){
                    Node width = findNodeByName(node.getChildren(),"width");
                    ViewRenderable widthView = (ViewRenderable) width.getRenderable();
                    if(widthView != null) ((TextView) widthView.getView().findViewById(R.id.test)).setText(String.format("%.2f", vector.x));
                }
                double volumeValue = 0;
                if(node.getName() == "cylinder"){
                    double d = vector.x/2;
                    double h = vector.y;
                    volumeValue = Math.PI * Math.pow(d,2) * h;
                }else if(node.getName() == "sphere"){
                    double r = vector.x/2;
                    volumeValue = (4/3)*Math.PI*Math.pow(r,3);
                }
                if(node.getName() == "pyramid"){
                    double l = vector.x;
                    double w = vector.y;
                    double h = vector.z;
                    volumeValue = (l*w*h)/3;
                }

                TextView v = (TextView) findViewById(R.id.volume);
                v.setText(String.format("Volume=%.2fcm\u00B3", volumeValue));
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

        Node diameter = new Node();
        diameter.setParent(model);
        diameter.setEnabled(false);
        diameter.setLocalPosition(new Vector3(0.0f, (float)dimensions.get(2)/100+0.04f, 0.0f));
        diameter.setLocalScale(new Vector3(0.4f,0.4f,0.4f));
        diameter.setName("diameter");
        ViewRenderable.builder()
                        .setView(this, R.layout.view_model_title)
                        .build()
                        .thenAccept(renderable -> {
                                ((TextView) renderable.getView().findViewById(R.id.test)).setText(Float.toString(dimensions.get(0)));
                                diameter.setRenderable(renderable);
                        }).exceptionally(throwable -> {
                            Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                            return null;
                        });

        Node height = new Node();
        height.setParent(model);
        height.setEnabled(false);
        height.setLocalPosition(new Vector3((float)dimensions.get(0)/100, ((float)dimensions.get(2)/100)/2-0.04f, 0.0f));
        height.setLocalScale(new Vector3(0.4f,0.4f,0.4f));
        height.setName("height");
        ViewRenderable.builder()
                        .setView(this, R.layout.view_model_title)
                        .build()
                        .thenAccept(renderable -> {
                                ((TextView) renderable.getView().findViewById(R.id.test)).setText(Float.toString(dimensions.get(2)));
                                height.setRenderable(renderable);
                        }).exceptionally(throwable -> {
                            Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                            return null;
                        });

        Node length = new Node();
        length.setParent(model);
        length.setEnabled(false);
        length.setLocalPosition(new Vector3((float)dimensions.get(0)/100, -0.08f, 0.0f));
        length.setLocalRotation(Quaternion.axisAngle(new Vector3(0f,1f,0f),90));
        length.setLocalScale(new Vector3(0.4f,0.4f,0.4f));
        length.setName("length");
        ViewRenderable.builder()
                        .setView(this, R.layout.view_model_title)
                        .build()
                        .thenAccept(renderable -> {
                            ((TextView) renderable.getView().findViewById(R.id.test)).setText(Float.toString(dimensions.get(0)));
                            length.setRenderable(renderable);
                        }).exceptionally(throwable -> {
                            Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                            return null;
                        });

        Node width = new Node();
        width.setParent(model);
        width.setEnabled(false);
        width.setLocalPosition(new Vector3(0.0f, -0.08f, (float)dimensions.get(0)/100));
        width.setLocalScale(new Vector3(0.4f,0.4f,0.4f));
        width.setName("width");
        ViewRenderable.builder()
                        .setView(this, R.layout.view_model_title)
                        .build()
                        .thenAccept(renderable -> {
                            ((TextView) renderable.getView().findViewById(R.id.test)).setText(Float.toString(dimensions.get(1)));
                            width.setRenderable(renderable);
                        }).exceptionally(throwable -> {
                            Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                            return null;
                        });
        double volumeValue = 0;
        if(model.getName() == "cylinder"){
            double d = dimensions.get(0).doubleValue();
            double h = dimensions.get(2).doubleValue();
            volumeValue = Math.PI * Math.pow(d/2,2) * h;
        }else if(model.getName() == "sphere"){
            double r = dimensions.get(0).doubleValue()/2;
            volumeValue = (4/3)*Math.PI*Math.pow(r,3);
        }
        if(model.getName() == "pyramid"){
            double l = dimensions.get(0).doubleValue();
            double w = dimensions.get(1).doubleValue();
            double h = dimensions.get(2).doubleValue();
            volumeValue = (l*w*h)/3;
        }

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
}
