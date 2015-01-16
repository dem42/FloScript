package com.premature.floscript.scripts.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.premature.floscript.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.view.View.LAYER_TYPE_SOFTWARE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScriptingFragment.OnScriptingFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScriptingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class ScriptingFragment extends Fragment {
    private static final String TAG = "SCRIPT_FRAG";
    private static final String PINNED_TEXT = "Unpin";
    private static final String UNPINNED_TEXT = "Pin";


    private LogicBlockUiElement mLogicBlockElement;
    private DiamondUiElement mDiamondElement;
    private ArrowUiElement mArrowElement;

    private OnScriptingFragmentInteractionListener mListener;

    @InjectView(R.id.script_editor)
    DiagramEditorView mDiagramEditorView;
    @InjectView(R.id.logic_elem_btn)
    Button mLogicElemBtn;
    @InjectView(R.id.diamond_elem_btn)
    Button mDiamondElemBtn;
    @InjectView(R.id.arrow_elem_btn)
    Button mArrowElemBtn;
    @InjectView(R.id.pin_btn)
    Button mPinUnpinBtn;

    private float mDensity;
    private StickyButtonCoordinator mBtnCoordinator;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScriptingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScriptingFragment newInstance(String param1, String param2) {
        ScriptingFragment fragment = new ScriptingFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ScriptingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
        init();
    }

    private void init() {
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mLogicBlockElement = new LogicBlockUiElement(null, (int)(40 * mDensity), (int)(40 * mDensity));
        this.mDiamondElement = new DiamondUiElement(null, (int)(40 * mDensity), (int)(40 * mDensity));
        this.mArrowElement = new ArrowUiElement(null, (int)(40 * mDensity), (int)(40 * mDensity));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_scripting, container, false);
        ButterKnife.inject(this, view);
        initButtons();
        return view;
    }

    private void initButtons() {
        mBtnCoordinator = new StickyButtonCoordinator();
        mDiagramEditorView.setOnDiagramEditorListener(mBtnCoordinator);

        mLogicElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mLogicElemBtn.setCompoundDrawables(mLogicBlockElement.getDrawable(), null, null, null);
        StickyButtonOnTouchListener logicListener = new StickyButtonOnTouchListener(mLogicElemBtn,
                mDiagramEditorView, mBtnCoordinator) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onLogicElementClicked();
            }
        };
        mLogicElemBtn.setOnTouchListener(logicListener);
        mBtnCoordinator.registerElementButtonListener(logicListener);

        mDiamondElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mDiamondElemBtn.setCompoundDrawables(mDiamondElement.getDrawable(), null, null, null);
        StickyButtonOnTouchListener diamondListener = new StickyButtonOnTouchListener(mDiamondElemBtn,
                mDiagramEditorView, mBtnCoordinator) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onDiamondElementClicked();
            }
        };
        mDiamondElemBtn.setOnTouchListener(diamondListener);
        mBtnCoordinator.registerElementButtonListener(diamondListener);

        mArrowElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mArrowElemBtn.setCompoundDrawables(mArrowElement.getDrawable(), null, null, null);
        StickyButtonOnTouchListener arrowListener = new StickyButtonOnTouchListener(mArrowElemBtn,
                mDiagramEditorView, mBtnCoordinator) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onArrowClicked();
            }
        };
        mArrowElemBtn.setOnTouchListener(arrowListener);
        mBtnCoordinator.registerElementButtonListener(arrowListener);

        mPinUnpinBtn.setText(UNPINNED_TEXT);
        StickyButtonOnTouchListener pinUnpinListener = new StickyButtonOnTouchListener(mPinUnpinBtn,
                mDiagramEditorView, mBtnCoordinator) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.pinningStateToggled();
            }
        };
        mPinUnpinBtn.setOnTouchListener(pinUnpinListener);
        mBtnCoordinator.setPinUButton(mPinUnpinBtn);
        mBtnCoordinator.setPinUnpinListener(pinUnpinListener);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onScriptingFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnScriptingFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnScriptingFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onScriptingFragmentInteraction(Uri uri);
    }

    /**
     * This class turns our buttons into stateful push buttons.
     * </p>
     * This means that they can no longer register click events, because this class consumes all touch
     * events. For that reason, a user of this class who desires custom behaviour for onClick events
     * should extend this class and place the logic inside the {@link #doOnClick()} method
     */
    private static class StickyButtonOnTouchListener implements View.OnTouchListener {
        boolean isPressed = false;
        private final Button mPressableElement;
        private final StickyButtonCoordinator mBtnCoordinator;
        protected final OnElementSelectorListener mOnElementSelectorListener;

        public StickyButtonOnTouchListener(Button logicElemBtn, OnElementSelectorListener listener, StickyButtonCoordinator btnCoordinator) {
            this.mPressableElement = logicElemBtn;
            this.mOnElementSelectorListener = listener;
            this.mBtnCoordinator = btnCoordinator;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mBtnCoordinator.isAllowedToToggle(this) && event.getAction() == MotionEvent.ACTION_DOWN) {
                isPressed = !isPressed;
                mPressableElement.setPressed(isPressed);
                doOnClick();
            }
            return true; //consumed .. don't send to any other listeners
        }

        public boolean isPressed() {
            return isPressed;
        }

        public void setPressed(boolean isPressed) {
            mPressableElement.setPressed(isPressed);
            this.isPressed = isPressed;
        }

        public void doOnClick() {
            Log.d(TAG, "Clicked " + mPressableElement);
        }
    }

    /**
     * This class acts as a nexus for the communication of sticky button listeners
     * </p>
     * The main task of this class is to coordinate that no more than one touch element
     * has been pressed and also to receive callbacks from the diagram editor about element placement
     * or element selection which affects the pin-unpin button.
     */
    private static class StickyButtonCoordinator implements OnDiagramEditorListener{
        private List<StickyButtonOnTouchListener> mElementButtons = new ArrayList<>();
        private StickyButtonOnTouchListener mPinUnpinListener;
        private Button mPinUButton;

        @Override
        public void onElementSelected(DiagramElement<?> element) {
            if (mPinUButton != null) {
                mPinUButton.setText(element.isPinned() ? PINNED_TEXT : UNPINNED_TEXT);
            }
        }

        @Override
        public void onElementPlaced() {
            for (StickyButtonOnTouchListener listener : mElementButtons) {
                listener.setPressed(false);
            }
        }

        public void registerElementButtonListener(StickyButtonOnTouchListener elementBtnListener) {
            mElementButtons.add(elementBtnListener);
        }

        public void setPinUnpinListener(StickyButtonOnTouchListener PinUnpinListener) {
            this.mPinUnpinListener = PinUnpinListener;
        }

        public void setPinUButton(Button PinUButton) {
            this.mPinUButton = PinUButton;
        }

        public boolean isAllowedToToggle(StickyButtonOnTouchListener stickyButtonOnTouchListener) {
            if (stickyButtonOnTouchListener == mPinUnpinListener) {
                // we do identity comparison here
                return true;
            }
            if (!stickyButtonOnTouchListener.isPressed()) {
                for (StickyButtonOnTouchListener listener : mElementButtons) {
                    if (listener.isPressed()) {
                        return false;
                    }
                }
            }
            // we are always allowed to release a pressed button
            // or we are allowed to press if no other element button is currently pressed
            return true;
        }
    }

}
