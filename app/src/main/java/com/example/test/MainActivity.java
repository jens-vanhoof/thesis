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
            List<Node> children = new ArrayList<>(arFragment.getArSceneView().getScene().getChildren());
            for (Node node : children) {
                if (node instanceof AnchorNode) {
                    if (((AnchorNode) node).getAnchor() != null) {
                        ((AnchorNode) node).getAnchor().detach();
                        arFragment.getArSceneView().getScene().removeChild(node);
                    }
                }

            }
            if (toggleMethod.isChecked()){
                hide.setVisibility(View.VISIBLE);
                if(source=="cylinder"||source=="pyramid"){
                    LinearLayout info = findViewById(R.id.info);
                    info.setOrientation(LinearLayout.VERTICAL);
                }
            }else{
                if(source=="cylinder"||source=="pyramid"){
                    LinearLayout info = findViewById(R.id.info);
                    info.setOrientation(LinearLayout.HORIZONTAL);
                }
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
            TextView formula = findViewById(R.id.formula);
            Switch toggleMethod = findViewById(R.id.toggleMethod);
            Vector3 refactor = new Vector3(x,x,z);
            if(toggleMethod.isChecked()){
                LinearLayout info = findViewById(R.id.info);
                info.setOrientation(LinearLayout.VERTICAL);
                formula.setText(String.format("A=2\u03C0rh+2\u03C0r\u00B2=2\u03C0%.2f%.2f+2\u03C0%.2f\u00B2=",refactor.x,refactor.z,refactor.x));
            }else{
                LinearLayout info = findViewById(R.id.info);
                info.setOrientation(LinearLayout.HORIZONTAL);
                formula.setText(String.format("V=\u03C0r\u00B2h=\u03C0%.2f\u00B2%.2f=",refactor.x,refactor.z));
            }

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
            TextView formula = findViewById(R.id.formula);
            Switch toggleMethod = findViewById(R.id.toggleMethod);
            Vector3 refactor = new Vector3(x,x,x);
            if(toggleMethod.isChecked()){
                formula.setText(String.format("A=lw+l\u221A[(w/2)\u00B2+h\u00B2]+w\u221A[(l/2)\u00B2+h\u00B2]=%1$.2f%2$.2f+l\u221A[(%2$.2f/2)\u00B2+%3$.2f\u00B2]+%2$.2f\u221A[(%1$.2f/2)\u00B2+%3$.2f\u00B2]=",refactor.x,refactor.y,refactor.z));
            }else{
                formula.setText(String.format("V=lwh/3=%.2f%.2f%.2f/3=",refactor.x,refactor.y,refactor.z));
            }
            loadModel(model);
            height = true;
            width = true;
            length = true;
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }
    public void cubeDialog(String model) {
        if(source=="cylinder" || source=="pyramid"){
            LinearLayout info = findViewById(R.id.info);
            info.setOrientation(LinearLayout.HORIZONTAL);
        }
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
            TextView formula = findViewById(R.id.formula);
            Switch toggleMethod = findViewById(R.id.toggleMethod);
            Vector3 refactor = new Vector3(x,x,x);
            if(toggleMethod.isChecked()){
                formula.setText(String.format("A=6a\u00B2=6%.2f\u00B2=",refactor.x));
            }else{
                formula.setText(String.format("V=a\u00B3=%.2f\u00B3=",refactor.x));
            }

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
                Switch toggleMethod = findViewById(R.id.toggleMethod);
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
                TextView formula = findViewById(R.id.formula);
                if(node.getName()=="cube"){
                    if(toggleMethod.isChecked()){
                        formula.setText(String.format("A=6a\u00B2=6*%.2f\u00B2=",vector.x));
                    }else{
                        formula.setText(String.format("V=a\u00B3=%.2f\u00B3=",vector.x));
                    }
                }
                if(node.getName() == "cylinder"){
                    if(toggleMethod.isChecked()){
                        formula.setText(String.format("A=2\u03C0rh+2\u03C0r\u00B2=2*\u03C0*%1$.2f*%2$.2f+2*\u03C0*%1$.2f\u00B2=",vector.x/2,vector.y));
                    }else{
                        formula.setText(String.format("V=\u03C0r\u00B2h=\u03C0*%1$.2f\u00B2*%2$.2f=",vector.x/2,vector.y));
                    }
                }
                if(node.getName() == "pyramid"){
                    if(toggleMethod.isChecked()){
                        formula.setText(String.format("A=lw+l\u221A[(w/2)\u00B2+h\u00B2]+w\u221A[(l/2)\u00B2+h\u00B2]=%1$.2f*%2$.2f+l*\u221A[(%2$.2f/2)\u00B2+%3$.2f\u00B2]+%2$.2f*\u221A[(%1$.2f/2)\u00B2+%3$.2f\u00B2]=",vector.z,vector.x,vector.y));
                    }else{
                        formula.setText(String.format("V=lwh/3=%.2f*%.2f*%.2f/3=",vector.z,vector.x,vector.y));
                    }
                }
                double measure = 0;
                String method;
                if(toggleMethod.isChecked()){
                    method = "Surface Area";
                }else{
                    method = "Volume";
                }
                if(arFragment.getArSceneView().getScene().getChildren().size() != 1){
                    if(toggleMethod.isChecked()){
                        measure = getSurfaceAreaValue(node.getName(),vector);
                    }else{
                        measure = getVolumeValue(node.getName(),vector);
                    }
                }

                TextView v = findViewById(R.id.measure);
                v.setText(String.format("%s=%.2fcm\u00B3",method, measure));
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
        Switch toggleMethod = findViewById(R.id.toggleMethod);
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

        double measure;
        String method;
        if(toggleMethod.isChecked()){
            measure = getSurfaceAreaValue(source,new Vector3((float)dimensions.get(0),(float)dimensions.get(2),(float)dimensions.get(1)));
            method = "Surface Area";
        }else{
            measure = getVolumeValue(source,new Vector3((float)dimensions.get(0),(float)dimensions.get(2),(float)dimensions.get(1)));
            method = "Volume";
        }

        TextView v = findViewById(R.id.measure);
        v.setText(String.format("%s=%.2fcm\u00B3",method, measure));
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

    private double getSurfaceAreaValue(String model, Vector3 vector) {
        if(model == "cylinder"){
            double r = vector.x/2;
            double h = vector.y;
            return 2*Math.PI * r * h + 2 * Math.PI * Math.pow(r,2);
        }else if(model == "cube"){
            double r = vector.x;
            return 6*Math.pow(r,2);
        }
        if(model == "pyramid"){
            double l = vector.x;
            double w = vector.y;
            double h = vector.z;
            return l*w + l * Math.sqrt((Math.pow(w/2,2)+Math.pow(h,2))) + w *Math.sqrt(Math.pow(l/2,2)+Math.pow(h,2));
        }
        return 0;
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
