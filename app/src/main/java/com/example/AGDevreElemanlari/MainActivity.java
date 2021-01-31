package com.example.AGDevreElemanlari;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AlertDialog;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private CustomArFragment arFragment;
    private boolean shouldAddModel=true;
    private ObjectAnimator objectAnimator=null;
    TextView hintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment=(CustomArFragment)getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::OnUpdateFrame);
        arFragment.getArSceneView().setLightDirectionUpdateEnabled(true);
        arFragment.getArSceneView().setLightEstimationEnabled(true);

         hintText=findViewById(R.id.ipucu);


        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);

        anim.setRepeatCount(Animation.INFINITE);
        hintText.startAnimation(anim);

    }
    //fotoğraf database'e bitmap olarak işlenir

    public boolean setupAugmentedImageDb(Config config, Session session){
        config.setFocusMode(Config.FocusMode.AUTO); //kamera odaklama

        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap bitmap=loadAugmentedImage();
        if(bitmap==null){
            return false;
        }
        augmentedImageDatabase=new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("arCover",bitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        session.configure(config);
        return true;
    }

    //algılanacak fotoğraf eklenir

    private Bitmap loadAugmentedImage(){
        try(InputStream is=getAssets().open("arCover.jpg")){
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e("ImageLoad","IO Exception while loading",e);

        }
        return null;
    }

    //  fotoğraf algılıyor


    private void OnUpdateFrame(FrameTime frameTime){
        Frame frame=arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> augmentedImages=frame.getUpdatedTrackables(AugmentedImage.class);
        for(AugmentedImage augmentedImage: augmentedImages){

            if(augmentedImage.getTrackingState()== TrackingState.TRACKING ){

                if(augmentedImage.getName().equals("arCover") && shouldAddModel){
                    mainCard(arFragment,
                            augmentedImage.createAnchor(augmentedImage.getCenterPose()),augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                    shouldAddModel=false;
                    hintText.setText("");
                }
            }
        }
    }
    private void mainCard(ArFragment fragment,Anchor anchor,Anchor anchor1){
        ViewRenderable.builder()
                .setView(this, R.layout.maincard)
                .build()
                .thenAccept(
                        (renderable) -> {
                            AnchorNode anchorNode=new AnchorNode(anchor);

                            Node node=new Node();
                            Node cardNode=new Node();
                            TransformableNode modelNode=new TransformableNode(arFragment.getTransformationSystem());

                            node.setRenderable(renderable);
                            node.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                            node.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                            node.setLocalPosition(new Vector3(-0.3f, 0.1f, 0.1f));
                            node.setParent(anchorNode);
                            fragment.getArSceneView().getScene().addChild(anchorNode);
                            fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                            Button pasifDevre=(Button)renderable.getView().findViewById(R.id.pasifDevreElemanları);
                            Button aktifDevre=(Button)renderable.getView().findViewById(R.id.aktifDevreElemanları);
                            Button motorBtn=(Button)renderable.getView().findViewById(R.id.motor);

                            pasifDevre.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    node.setRenderable(null);
                                    passiveObjects(fragment,anchor1,cardNode);
                                }
                            });
                            aktifDevre.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    node.setRenderable(null);
                                    activeObjects(fragment,anchor1,cardNode);
                                }
                            });
                            motorBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    node.setRenderable(null);
                                    motorMenu(fragment,anchor1,cardNode);
                                }
                            });
                        })
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }
    private void motorMenu(ArFragment fragment, Anchor anchor, Node cardNode){
        AnchorNode anchorNode=new AnchorNode(anchor);
        ViewRenderable.builder()
                .setView(this, R.layout.motor_menu)
                .build()
                .thenAccept(
                        (renderable) -> {

                            cardNode.setRenderable(renderable);
                            cardNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                            cardNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                            cardNode.setLocalPosition(new Vector3(-0.3f, 0.1f, 0.1f));
                            cardNode.setParent(anchorNode);
                            fragment.getArSceneView().getScene().addChild(anchorNode);
                            fragment.getArSceneView().getScene().getCamera().getWorldPosition();


                            Node infoNode=new Node();
                            Node detayNode=new Node();
                            Node terminal=new Node();
                            Node saft=new Node();
                            Node fan=new Node();
                            Node rotor=new Node();


                            // Node modelNode=new Node();
                            TransformableNode modelNode=new TransformableNode(arFragment.getTransformationSystem());

                            Button geriBtn=(Button)renderable.getView().findViewById(R.id.geri);
                            Button asenkronBtn=(Button)renderable.getView().findViewById(R.id.asenkron);
                            Button senkronBtn=(Button)renderable.getView().findViewById(R.id.senkron);


                            asenkronBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    terminal.setRenderable(null);
                                    saft.setRenderable(null);
                                    fan.setRenderable(null);
                                    rotor.setRenderable(null);
                                    infoViewer(arFragment,anchor,R.string.asenkron,infoNode);
                                    asenkronMotorPlace(arFragment,anchor,Uri.parse("asenkron.sfb"),modelNode,detayNode,terminal,saft,fan,rotor);                                }
                            });
                            senkronBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    terminal.setRenderable(null);
                                    saft.setRenderable(null);
                                    fan.setRenderable(null);
                                    rotor.setRenderable(null);
                                    infoViewer(arFragment,anchor,R.string.senkron,infoNode);
                                    senkronMotorPlace(arFragment,anchor,Uri.parse("senkron.sfb"),modelNode,detayNode,saft,fan,rotor);
                                }
                            });

                            geriBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    infoNode.setRenderable(null);
                                    modelNode.setRenderable(null);
                                    cardNode.setRenderable(null);
                                    detayNode.setRenderable(null);
                                    terminal.setRenderable(null);
                                    saft.setRenderable(null);
                                    fan.setRenderable(null);
                                    rotor.setRenderable(null);

                                    mainCard(fragment,anchor,anchor);
                                }
                            });
                        })
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });


    }
    private void activeObjects(ArFragment fragment, Anchor anchor,Node cardNode){
        AnchorNode anchorNode=new AnchorNode(anchor);
        ViewRenderable.builder()
                .setView(this, R.layout.active_menu_items)
                .build()
                .thenAccept(
                        (renderable) -> {

                            cardNode.setRenderable(renderable);
                            cardNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                            cardNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                            cardNode.setLocalPosition(new Vector3(-0.3f, 0.1f, 0.1f));
                            cardNode.setParent(anchorNode);
                            fragment.getArSceneView().getScene().addChild(anchorNode);
                            fragment.getArSceneView().getScene().getCamera().getWorldPosition();


                            Node infoNode=new Node();
                            // Node modelNode=new Node();
                            TransformableNode modelNode=new TransformableNode(arFragment.getTransformationSystem());


                            Button diodeBtn=(Button)renderable.getView().findViewById(R.id.diode);
                            Button transitorBtn=(Button)renderable.getView().findViewById(R.id.transistor);
                            Button integratedBtn=(Button)renderable.getView().findViewById(R.id.integrated);
                            Button opampBtn=(Button)renderable.getView().findViewById(R.id.opamp);
                            Button geriBtn=(Button)renderable.getView().findViewById(R.id.geri);

                            diodeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    infoViewer(arFragment,anchor,R.string.diyot,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("diyot.sfb"),modelNode);
                                }
                            });

                            transitorBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    infoViewer(arFragment,anchor,R.string.transistor,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("transistor.sfb"),modelNode);
                                }
                            });

                            integratedBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    infoViewer(arFragment,anchor,R.string.entegre,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("entegre.sfb"),modelNode);
                                }
                            });

                            opampBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    infoViewer(arFragment,anchor,R.string.opamp,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("opamp.sfb"),modelNode);
                                }
                            });
                            geriBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    infoNode.setRenderable(null);
                                    modelNode.setRenderable(null);
                                    cardNode.setRenderable(null);
                                    mainCard(fragment,anchor,anchor);
                                }
                            });
                        })
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    private void passiveObjects(ArFragment fragment, Anchor anchor,Node cardNode){
        AnchorNode anchorNode=new AnchorNode(anchor);
        ViewRenderable.builder()
                .setView(this, R.layout.passive_menu_items)
                .build()
                .thenAccept(
                        (renderable) -> {
                                cardNode.setRenderable(renderable);
                                cardNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                                cardNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                                cardNode.setLocalPosition(new Vector3(-0.3f, 0.1f, 0.1f));
                                cardNode.setParent(anchorNode);
                                fragment.getArSceneView().getScene().addChild(anchorNode);
                                fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                                Node infoNode=new Node();
                                TransformableNode modelNode=new TransformableNode(arFragment.getTransformationSystem());

                            Button resistorBtn = (Button) renderable.getView().findViewById(R.id.resistor);
                            Button capasitorBtn = (Button) renderable.getView().findViewById(R.id.capasitor);
                            Button inductorBtn=(Button)renderable.getView().findViewById(R.id.inductor);
                            Button thermistorBtn=(Button)renderable.getView().findViewById(R.id.thermistor);
                            Button ldrBtn=(Button)renderable.getView().findViewById(R.id.ldr);
                            Button potentiometerBtn=(Button)renderable.getView().findViewById(R.id.potentiometer);
                            Button geriBtn=(Button)renderable.getView().findViewById(R.id.geri);
                            resistorBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    infoViewer(arFragment,anchor,R.string.direnc,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("resistor.sfb"),modelNode);

                                }
                            });
                            capasitorBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    infoViewer(arFragment,anchor,R.string.kapasitor,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("kapasitor.sfb"),modelNode);



                                }
                            });
                            inductorBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    infoViewer(arFragment,anchor,R.string.bobin,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("bobin.sfb"),modelNode);
                                }
                            });
                            thermistorBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    infoViewer(arFragment,anchor,R.string.termistor,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("termistor.sfb"),modelNode);
                                }
                            });

                            ldrBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    infoViewer(arFragment,anchor,R.string.ldr,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("ldr.sfb"),modelNode);
                                }
                            });

                            potentiometerBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    infoViewer(arFragment,anchor,R.string.potansiyometre,infoNode);
                                    placeModel(arFragment,anchor,Uri.parse("potansiyometre.sfb"),modelNode);
                                }
                            });

                            geriBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    infoNode.setRenderable(null);
                                    modelNode.setRenderable(null);
                                    cardNode.setRenderable(null);
                                    mainCard(fragment,anchor,anchor);
                                }
                            });
                        })
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

    }
    private void infoViewer(ArFragment fragment, Anchor infoAnchor,Integer layout,Node node1){
        AnchorNode infoAnchorNode=new AnchorNode(infoAnchor);
        ViewRenderable.builder()
                .setView(fragment.getContext(), R.layout.infocardviewer)
                .build()
                .thenAccept(
                        (renderable) -> {

                            node1.setRenderable(renderable);
                            node1.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                            node1.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                            node1.setLocalPosition(new Vector3(0f, 0.1f, 0.1f));
                            node1.setParent(infoAnchorNode);
                            fragment.getArSceneView().getScene().addChild(infoAnchorNode);
                            fragment.getArSceneView().getScene().getCamera().getWorldPosition();
                            TextView info=(TextView)renderable.getView().findViewById(R.id.resistorInfo);
                            info.setText(layout);

                        })
                .exceptionally(
                        throwable -> {
                            AlertDialog.Builder builder=new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Error!");
                            AlertDialog dialog=builder.create();
                            dialog.show();
                            return null;
                        });

        }


    private void placeModel(ArFragment fragment, Anchor modelAnchor,Uri model,TransformableNode node2){

        AnchorNode modelAnchorNode = new AnchorNode(modelAnchor);
        ModelRenderable.builder()
                .setSource(fragment.getContext(),model)
                .build()
                .thenAccept( modelRenderable -> {


                        node2.setRenderable(modelRenderable);
                        node2.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                        node2.setLocalPosition(new Vector3(0.3f, 0.1f, 0.1f));//-0.7
                        node2.getRotationController().setEnabled(true);
                        node2.getScaleController().setEnabled(true);
                        node2.setParent(modelAnchorNode);
                        node2.getScaleController().setMinScale(0.0999f);
                        node2.getScaleController().setMaxScale(0.1000f);

                        node2.select();
                        fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                        fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                         startAnimation(node2);




                })
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog=builder.create();
                    dialog.show();

                    return null;
                });
    }
    private void asenkronMotorPlace(ArFragment fragment, Anchor modelAnchor,Uri model,TransformableNode node2,Node detailsNode,Node terminal,Node saft,Node fan, Node rotor){

        AnchorNode modelAnchorNode = new AnchorNode(modelAnchor);
        ModelRenderable.builder()
                .setSource(fragment.getContext(),model)
                .build()
                .thenAccept( modelRenderable -> {



                    node2.setRenderable(modelRenderable);
                    node2.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    node2.setLocalPosition(new Vector3(0.4f, 0.1f, 0.1f));
                    node2.getRotationController().setEnabled(true);
                    node2.getScaleController().setEnabled(true);
                    node2.setParent(modelAnchorNode);
                    node2.getScaleController().setMinScale(0.0999f);
                    node2.getScaleController().setMaxScale(0.1000f);

                    node2.select();
                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();


                })
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog=builder.create();
                    dialog.show();

                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.details)
                .build()
                .thenAccept(
                        (renderable) -> {


                            detailsNode.setRenderable(renderable);
                            detailsNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                            detailsNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                            detailsNode.setLocalPosition(new Vector3(0.42f, 0.35f, 0.1f));
                            detailsNode.setParent(modelAnchorNode);
                            fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                            fragment.getArSceneView().getScene().getCamera().getWorldPosition();


                            Button detailsBtn=(Button)renderable.getView().findViewById(R.id.detaylar);
                            Button deleteBtn=(Button)renderable.getView().findViewById(R.id.deletedetails);

                            detailsBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                 asenkronMotorHints(arFragment,modelAnchor,terminal,saft,fan,rotor);
                                 node2.setRenderable(null);
                                 placeObject(arFragment,modelAnchor,Uri.parse("asenkronsplit.sfb"),node2);


                                }
                            });
                            deleteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    terminal.setRenderable(null);
                                    saft.setRenderable(null);
                                    fan.setRenderable(null);
                                    rotor.setRenderable(null);
                                    node2.setRenderable(null);
                                    placeObject(arFragment,modelAnchor,Uri.parse("asenkron.sfb"),node2);

                                }
                            });




                        })
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

    }
    private void senkronMotorPlace(ArFragment fragment, Anchor modelAnchor,Uri model,TransformableNode node2,Node detailsNode,Node saft,Node fan, Node rotor){

        AnchorNode modelAnchorNode = new AnchorNode(modelAnchor);
        ModelRenderable.builder()
                .setSource(fragment.getContext(),model)
                .build()
                .thenAccept( modelRenderable -> {



                    node2.setRenderable(modelRenderable);
                    node2.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    node2.setLocalPosition(new Vector3(0.4f, 0.1f, 0.1f));
                    node2.getRotationController().setEnabled(true);
                    node2.getScaleController().setEnabled(true);
                    node2.setParent(modelAnchorNode);
                    node2.getScaleController().setMinScale(0.0999f);
                    node2.getScaleController().setMaxScale(0.1000f);

                    node2.select();
                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();


                })
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog=builder.create();
                    dialog.show();

                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.details)
                .build()
                .thenAccept(
                        (renderable) -> {


                            detailsNode.setRenderable(renderable);
                            detailsNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                            detailsNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                            detailsNode.setLocalPosition(new Vector3(0.42f, 0.35f, 0.1f));
                            detailsNode.setParent(modelAnchorNode);
                            fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                            fragment.getArSceneView().getScene().getCamera().getWorldPosition();


                            Button detailsBtn=(Button)renderable.getView().findViewById(R.id.detaylar);
                            Button deleteBtn=(Button)renderable.getView().findViewById(R.id.deletedetails);

                            detailsBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    senkronMotorHints(arFragment,modelAnchor,saft,fan,rotor);
                                    node2.setRenderable(null);
                                    placeObject(arFragment,modelAnchor,Uri.parse("senkronsplit.sfb"),node2);


                                }
                            });
                            deleteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    saft.setRenderable(null);
                                    fan.setRenderable(null);
                                    rotor.setRenderable(null);
                                    node2.setRenderable(null);
                                    placeObject(arFragment,modelAnchor,Uri.parse("senkron.sfb"),node2);

                                }
                            });




                        })
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

    }
    public void senkronMotorHints(ArFragment fragment, Anchor modelAnchor, Node saft, Node fan, Node rotor){
        AnchorNode modelAnchorNode = new AnchorNode(modelAnchor);

        ViewRenderable.builder()
                .setView(this,R.layout.asenkron_saft)
                .build()
                .thenAccept(viewRenderable -> {
                    saft.setRenderable(viewRenderable);
                    saft.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    saft.setLocalPosition(new Vector3(0.24f, 0.1f, 0.1f));
                    saft.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
                    saft.setParent(modelAnchorNode);

                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                });
        ViewRenderable.builder()
                .setView(this,R.layout.asenkronfan)
                .build()
                .thenAccept(viewRenderable -> {
                    fan.setRenderable(viewRenderable);
                    fan.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    fan.setLocalPosition(new Vector3(0.66f, 0.13f, 0.1f));
                    fan.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
                    fan.setParent(modelAnchorNode);

                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                });
        ViewRenderable.builder()
                .setView(this,R.layout.asenkron_rotor_stator)
                .build()
                .thenAccept(viewRenderable -> {
                    rotor.setRenderable(viewRenderable);
                    rotor.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    rotor.setLocalPosition(new Vector3(0.417f, 0.05f, 0.1f));
                    rotor.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
                    rotor.setParent(modelAnchorNode);

                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                });
    }
    public void asenkronMotorHints(ArFragment fragment, Anchor modelAnchor,Node terminal, Node saft, Node fan, Node rotor){
        AnchorNode modelAnchorNode = new AnchorNode(modelAnchor);

        ViewRenderable.builder()
                .setView(this,R.layout.terminalbox)
                .build()
                .thenAccept(viewRenderable -> {
                    terminal.setRenderable(viewRenderable);
                    terminal.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    terminal.setLocalPosition(new Vector3(0.413f, 0.25f, 0.1f));
                    terminal.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
                    terminal.setParent(modelAnchorNode);

                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                });
        ViewRenderable.builder()
                .setView(this,R.layout.asenkron_saft)
                .build()
                .thenAccept(viewRenderable -> {
                    saft.setRenderable(viewRenderable);
                    saft.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    saft.setLocalPosition(new Vector3(0.255f, 0.1f, 0.1f));
                    saft.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
                    saft.setParent(modelAnchorNode);

                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                });
        ViewRenderable.builder()
                .setView(this,R.layout.asenkronfan)
                .build()
                .thenAccept(viewRenderable -> {
                    fan.setRenderable(viewRenderable);
                    fan.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    fan.setLocalPosition(new Vector3(0.647f, 0.13f, 0.1f));
                    fan.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
                    fan.setParent(modelAnchorNode);

                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                });
        ViewRenderable.builder()
                .setView(this,R.layout.asenkron_rotor_stator)
                .build()
                .thenAccept(viewRenderable -> {
                    rotor.setRenderable(viewRenderable);
                    rotor.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    rotor.setLocalPosition(new Vector3(0.413f, 0.05f, 0.1f));
                    rotor.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
                    rotor.setParent(modelAnchorNode);

                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();

                });
    }
    private void placeObject(ArFragment fragment, Anchor anchor, Uri model,Node node1){
        ModelRenderable.builder()
                .setSource(fragment.getContext(),model)
                .build()
                .thenAccept( modelRenderable -> {
                    AnchorNode modelAnchorNode = new AnchorNode(anchor);



                    node1.setRenderable(modelRenderable);
                    node1.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0));
                    node1.setLocalPosition(new Vector3(0.4f, 0.1f, 0.1f));
                    node1.setParent(modelAnchorNode);
                    fragment.getArSceneView().getScene().addChild(modelAnchorNode);
                    fragment.getArSceneView().getScene().getCamera().getWorldPosition();



                })
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog=builder.create();
                    dialog.show();

                    return null;
                });

    }
    private static ObjectAnimator createAnimator(){
        Quaternion[] orientations = new Quaternion[4];


        Quaternion baseOrientation = Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 0);
        for (int i = 0; i < orientations.length; i++) {
            float angle = i * 360 / (orientations.length - 1);
            Quaternion orientation = Quaternion.axisAngle(new Vector3(0f, 1f, 0f), angle);
            orientations[i] = Quaternion.multiply(baseOrientation, orientation);
        }


        ObjectAnimator objectAnimator = new ObjectAnimator();

        //  objectAnimator.setObjectValues(orientation1, orientation2, orientation3, orientation4);
        objectAnimator.setObjectValues((Object[]) orientations);

        // Next, give it the localRotation property.
        objectAnimator.setPropertyName("localRotation");

        // Use Sceneform's QuaternionEvaluator.
        objectAnimator.setEvaluator(new QuaternionEvaluator());

        //  Allow orbitAnimation to repeat forever
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setAutoCancel(true);
        return objectAnimator;
    }
    private void startAnimation(TransformableNode node) {


        objectAnimator = createAnimator();
        objectAnimator.setTarget(node);
        objectAnimator.setDuration(getAnimationDuration(1/10f));
        objectAnimator.start();


    }

    private long getAnimationDuration(Float a) {
        Log.i("ASIL DEĞER", Float.toString(a));
        return (long) (300 * 360  / (a * (90f * 1f)) );

    }

}
