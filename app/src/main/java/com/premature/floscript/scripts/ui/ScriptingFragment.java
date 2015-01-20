package com.premature.floscript.scripts.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.premature.floscript.R;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.FloDbHelper;
import com.premature.floscript.scripts.logic.DiagramToScriptCompiler;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.logic.ScriptCompilationException;
import com.premature.floscript.scripts.logic.ScriptEngine;

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
public final class ScriptingFragment extends Fragment implements SaveDialog.OnSaveDialogListener,
        LoadDialog.OnLoadDialogListener {
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
    private DiagramDao mDiagramDao;
    private DiagramToScriptCompiler mCompiler;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * We use this instead of a overloaded constructor because
     * android will call the no argument constructor when it decides to
     * recreate our fragment later. So we shouldn't use those all over the place
     * instead we should pass arguments through the setArguments method (these will
     * be retained between recreations)
     *
     * @return A new instance of fragment ScriptingFragment.
     */
    public static ScriptingFragment newInstance() {
        ScriptingFragment fragment = new ScriptingFragment();
        Bundle args = new Bundle();
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
            // looks like there may be multiple onCreate calls but one will have the arguments
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
        init();

        // need to call this if we want to manipulate the options menu
        setHasOptionsMenu(true);
    }

    private void init() {
        this.mCompiler = new DiagramToScriptCompiler(getActivity());
        this.mDiagramDao = new DiagramDao(getActivity());
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mLogicBlockElement = new LogicBlockUiElement(null, (int) (40 * mDensity), (int) (40 * mDensity));
        this.mDiamondElement = new DiamondUiElement(null, (int) (40 * mDensity), (int) (40 * mDensity));
        this.mArrowElement = new ArrowUiElement(null, (int) (40 * mDensity), (int) (40 * mDensity));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "inflating our menu " + menu);
        inflater.inflate(R.menu.menu_scripts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_save:
                saveDiagram();
                return true;
            case R.id.action_load:
                loadDiagram();
                return true;
            case R.id.action_compile:
                Log.d(TAG, "Compiling");
                compileDiagram();
                return true;
            case R.id.action_test:
                Log.d(TAG, "Testing code");
                compileAndRunDiagram();
                return true;
            case R.id.action_admin:
                Log.d(TAG, "admin code");
                adminCode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO: get rid of adming stuff
    private void adminCode() {
        FloDbHelper mDb = FloDbHelper.getInstance(getActivity());
        mDb.wipe();
    }

    private void compileAndRunDiagram() {
        try {
            Script script = mCompiler.compile(mDiagramEditorView.getDiagram());
            String result = ScriptEngine.runScript(script);
            TextPopupDialog.showPopup(getActivity().getSupportFragmentManager(), script.getSourceCode() +
                    "\n\nWith result: " + result);
        } catch (ScriptCompilationException e) {
            TextPopupDialog.showPopup(getActivity().getSupportFragmentManager(), e.getMessage());
            Log.e(TAG, "compile exp", e);
        }
    }

    private void compileDiagram() {
        DiagramToScriptCompiler compiler = new DiagramToScriptCompiler(getActivity());

        try {
            Script script = compiler.compile(mDiagramEditorView.getDiagram());
            TextPopupDialog.showPopup(getActivity().getSupportFragmentManager(), script.getSourceCode());
        } catch (ScriptCompilationException e) {
            TextPopupDialog.showPopup(getActivity().getSupportFragmentManager(), e.getMessage());
            Log.e(TAG, "compile exp", e);
        }
    }

    private void loadDiagram() {
        LoadDialog dialog = new LoadDialog();
        dialog.setTargetFragment(this, 1);
        dialog.show(getActivity().getSupportFragmentManager(), "load dialog");
    }

    private void saveDiagram() {
        SaveDialog dialog = new SaveDialog();
        dialog.setTargetFragment(this, 1);
        dialog.show(getActivity().getSupportFragmentManager(), "save dialog");
    }

    @Override
    public void loadClicked(String name) {
        new LoadTask(this).execute(name);
    }

    @Override
    public void saveClicked(String name) {
        final Diagram diagram = mDiagramEditorView.getDiagram();
        diagram.setName(name);
        try {
            Script compiledDiagram = mCompiler.compile(diagram);
            diagram.setCompiledDiagram(compiledDiagram);
        } catch (ScriptCompilationException e) {
            TextPopupDialog.showPopup(getActivity().getSupportFragmentManager(), "Failed to compile diagram. It will not be available" +
                    "as a job\n\nReason:" + e.getMessage());
        }
        new SaveTask(this).execute(diagram);
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

    // these methods are for the interaction with nested async task (dont want inner class asyncs)
    DiagramDao getDiagramDao() {
        return mDiagramDao;
    }
    // these methods are for the interaction with nested async task (dont want inner class asyncs)
    void showDiagram(Diagram diagram) {
        mDiagramEditorView.setDiagram(diagram);
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

    private static final class SaveTask extends AsyncTask<Diagram, Void, Boolean> {

        private final ScriptingFragment mFrag;

        private SaveTask(ScriptingFragment mFrag) {
            this.mFrag = mFrag;
        }

        @Override
        protected Boolean doInBackground(Diagram... params) {
            if (mFrag != null) {
                return mFrag.getDiagramDao().saveDiagram(params[0]);
            }
            else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (mFrag != null) {
                if (aBoolean) {
                    Toast.makeText(mFrag.getActivity(), "Diagram saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mFrag.getActivity(), "Failed to save diagram", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private static final class LoadTask extends AsyncTask<String, Void, Diagram> {

        private final ScriptingFragment mFrag;

        private LoadTask(ScriptingFragment mFrag) {
            this.mFrag = mFrag;
        }

        @Override
        protected Diagram doInBackground(String... params) {
            if (mFrag != null) {
                return mFrag.getDiagramDao().getDiagram(params[0]);
            }
            else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Diagram diagram) {
            if (mFrag != null) {
                mFrag.showDiagram(diagram);
            }
        }
    }



}
