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
public final class ScriptingFragment extends Fragment implements OnDiagramEditorListener {
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
        this.mLogicBlockElement = new LogicBlockUiElement((int)(40 * mDensity), (int)(40 * mDensity));
        this.mDiamondElement = new DiamondUiElement((int)(40 * mDensity), (int)(40 * mDensity));
        this.mArrowElement = new ArrowUiElement((int)(40 * mDensity), (int)(40 * mDensity));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_scripting, container, false);
        ButterKnife.inject(this, view);
        initButtons();
        mDiagramEditorView.setOnDiagramEditorListener(this);
        return view;
    }

    private void initButtons() {

        mLogicElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mLogicElemBtn.setCompoundDrawables(mLogicBlockElement.getDrawable(), null, null, null);
        mLogicElemBtn.setOnTouchListener(new StickyButtonOnTouchListener(mLogicElemBtn, mDiagramEditorView) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onDiamondElementClicked();
            }
        });

        mDiamondElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mDiamondElemBtn.setCompoundDrawables(mDiamondElement.getDrawable(), null, null, null);
        mDiamondElemBtn.setOnTouchListener(new StickyButtonOnTouchListener(mDiamondElemBtn, mDiagramEditorView) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onDiamondElementClicked();
            }
        });

        mArrowElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mArrowElemBtn.setCompoundDrawables(mArrowElement.getDrawable(), null, null, null);
        mArrowElemBtn.setOnTouchListener(new StickyButtonOnTouchListener(mArrowElemBtn, mDiagramEditorView) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onArrowClicked();
            }
        });

        mPinUnpinBtn.setText(UNPINNED_TEXT);
        mPinUnpinBtn.setOnTouchListener(new StickyButtonOnTouchListener(mPinUnpinBtn, mDiagramEditorView) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.pinningStateToggled();
            }
        });
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

    @Override
    public void onElementSelected(DiagramElement<?> element) {
        mPinUnpinBtn.setText(element.isPinned() ? PINNED_TEXT : UNPINNED_TEXT);
    }

    @Override
    public void onElementPlaced() {

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
     * This mOnElementSelectorListener turns our buttons into stateful push buttons
     */
    private static class StickyButtonOnTouchListener implements View.OnTouchListener {
        boolean isPressed = false;
        private final Button mPressableElement;
        private final OnElementSelectorListener mOnElementSelectorListener;

        public StickyButtonOnTouchListener(Button logicElemBtn, OnElementSelectorListener listener) {
            this.mPressableElement = logicElemBtn;
            this.mOnElementSelectorListener = listener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isPressed = !isPressed;
                mPressableElement.setPressed(isPressed);
            }
            return true; //consumed .. don't send to any other listeners
        }

        public void doOnClick() {
            Log.d(TAG, "Clicked " + mPressableElement);
        }
    }

}
