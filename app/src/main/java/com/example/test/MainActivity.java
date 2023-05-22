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
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    private ObjectAnimator unfold,fold,closed,open;
    private UUID currentId;
    private HashMap<UUID,String> animationStates = new HashMap<>();
    private HashMap<String,ObjectAnimator> animators = new HashMap<>();

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

        Button pyramid = findViewById(R.id.pyramid);
        Button cube  = findViewById(R.id.cube);
        Button cylinder = findViewById(R.id.cylinder);
        Switch toggleMethod = findViewById(R.id.toggleMethod);
        toggleMethod.setOnClickListener(view -> {
            LinearLayout hide = findViewById(R.id.hideSurfaceArea);
            if (toggleMethod.isChecked()){
                hide.setVisibility(View.VISIBLE);
            }
            else{
                hide.setVisibility(View.INVISIBLE);
            }
        });

        pyramid.setOnClickListener(view -> pyramidDialog("pyramid"));
        cube.setOnClickListener(view -> cubeDialog("cube"));
        cylinder.setOnClickListener(view -> cylinderDialog("cylinder"));
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
    public void cubeDialog(String model) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_cube, null);
        final TextInputEditText ribbeField = alertLayout.findViewById(R.id.ribbe);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Dimensions");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        //alert.setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show());

        alert.setPositiveButton("Done", (dialog, which) -> {
            dimensions.clear();
            int x = Integer.parseInt(ribbeField.getText().toString());
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
        length = false;
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
    private void updateDimensions(){
            Node node = arFragment.getTransformationSystem().getSelectedNode();
            if(node != null && node.getChildren() != null){
                Node model = node.getChildren().get(0);
                UUID nodeId = ((CustomTransformableNode) node).getIdentifier();
                if(currentId != nodeId){
                    currentId = nodeId;
                    updateModelAnimators();
                }
                Switch toggleFold = findViewById(R.id.toggleFold);
                if(animationStates.get(currentId) == "Open"){
                    toggleFold.setChecked(true);
                }
                if(animationStates.get(currentId) == "Closed"){
                    toggleFold.setChecked(false);
                }
                Vector3 vector = model.getWorldScale();
                updateTextFields(this.diameter, node, "diameter", vector.x);
                updateTextFields(this.height, node, "height", vector.y);
                updateTextFields(this.length, node, "length", vector.z);
                updateTextFields(this.width, node, "width", vector.x);
                double volumeValue = getVolumeValue(node.getName(),vector);

                TextView v = findViewById(R.id.volume);
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
        TransformableNode model = new CustomTransformableNode(arFragment.getTransformationSystem());
        model.setName(source);
        currentId = UUID.randomUUID();
        ((CustomTransformableNode) model).setIdentifier(currentId);
        Node modelNode = new Node();
        modelNode.setParent(model);
        RenderableInstance instance = modelNode.setRenderable(this.model);
        Box boundingBox = instance.getFilamentAsset().getBoundingBox();
        model.getScaleController().setMaxScale(100f);
        model.getScaleController().setMinScale(1f);
        Vector3 test = new Vector3((float)dimensions.get(0),(float)dimensions.get(2),(float)dimensions.get(1));
        if (boundingBox != null) {
            modelNode.setLocalScale(test);
        }
        model.setParent(anchorNode);

        RenderableInstance renderableInstance = model.getChildren().get(0).getRenderableInstance();
        ModelAnimator.ofAnimationFrame( renderableInstance, "Closed",this.model.getAnimationFrameRate()).start();
        animationStates.put(currentId,"Closed");
        createInitialModelAnimators(renderableInstance);


        Switch toggle = findViewById(R.id.toggleFold);
        toggle.setOnClickListener(view -> {
            if (toggle.isChecked()){
                toggle.setEnabled(false);
                closed.end();
                animationStates.put(currentId,"Unfold");
                animate(unfold, open);
                unfold.start();
            }
            else{
                toggle.setEnabled(false);
                open.end();
                animate(fold,closed);
                animationStates.put(currentId,"Fold");
                fold.start();
            }
        });

        model.select();

        Vector3 diameterPosition = new Vector3(0.0f, (float)dimensions.get(2)/100+0.04f, 0.0f);
        Node diameter = addNode(model,diameterPosition,"diameter",dimensions.get(0));

        Vector3 heightPosition = new Vector3((float)dimensions.get(0)/100, ((float)dimensions.get(2)/100)/2-0.04f, 0.0f);
        Node height = addNode(model,heightPosition,"height",dimensions.get(2));

        Vector3 lengthPosition = new Vector3((float)dimensions.get(0)/100, -0.08f, 0.0f);
        Node length = addNode(model,lengthPosition,"length",dimensions.get(0),Quaternion.axisAngle(new Vector3(0f,1f,0f),90));

        Vector3 widthPosition = new Vector3(0.0f, -0.08f, (float)dimensions.get(0)/100);
        Node width = addNode(model,widthPosition,"width",dimensions.get(1));

        double volumeValue = getVolumeValue(model.getName(),new Vector3((float)dimensions.get(0),(float)dimensions.get(2),(float)dimensions.get(1)));

        TextView v = findViewById(R.id.volume);
        v.setText(String.format("Volume=%.2fcm\u00B3", volumeValue));
        arFragment.getArSceneView().getScene().addOnUpdateListener(view -> updateDimensions());


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
    private void updateModelAnimators(){
        unfold = animators.get(currentId.toString()+",unfold");
        fold = animators.get(currentId.toString()+",fold");
        open = animators.get(currentId.toString()+",open");
        closed = animators.get(currentId.toString()+",closed");
    }
    private void createInitialModelAnimators(RenderableInstance renderableInstance) {
        unfold = ModelAnimator.ofAnimation(renderableInstance,"Unfold");
        unfold.setPropertyName("Unfold");
        open = ModelAnimator.ofAnimation(renderableInstance,"Open");
        open.setPropertyName("Open");
        fold = ModelAnimator.ofAnimation(renderableInstance,"Fold");
        fold.setPropertyName("Fold");
        closed = ModelAnimator.ofAnimation(renderableInstance,"Closed");
        closed.setPropertyName("Closed");
        animators.put(currentId.toString()+",unfold",unfold);
        animators.put(currentId.toString()+",open",open);
        animators.put(currentId.toString()+",fold",fold);
        animators.put(currentId.toString()+",closed",closed);
    }

    private void animate(ObjectAnimator startState, ObjectAnimator endState) {
        startState.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                endState.start();
                animationStates.put(currentId,endState.getPropertyName());// Start the 'open' animation
                findViewById(R.id.toggleFold).setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                // Animation canceled
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                // Animation repeated
                startState.end();
            }
        });

    }

    private double getVolumeValue(String model,Vector3 vector) {
        if(model == "cylinder"){
            double d = vector.x;
            double h = vector.y;
            return Math.PI * Math.pow(d/2,2) * h;
        }else if(model == "cube"){
            double r = vector.x;
            return Math.pow(r,3);
        }
        if(model == "pyramid"){
            double l = vector.x;
            double w = vector.y;
            double h = vector.z;
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
