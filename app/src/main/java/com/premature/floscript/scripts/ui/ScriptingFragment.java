package com.premature.floscript.scripts.ui;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.premature.floscript.R;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.FloDbHelper;
import com.premature.floscript.scripts.logic.DiagramToScriptCompiler;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.logic.ScriptCompilationException;
import com.premature.floscript.scripts.logic.ScriptEngine;
import com.premature.floscript.scripts.ui.diagram.ArrowUiElement;
import com.premature.floscript.scripts.ui.diagram.Diagram;
import com.premature.floscript.scripts.ui.diagram.DiagramEditorView;
import com.premature.floscript.scripts.ui.diagram.DiagramElement;
import com.premature.floscript.scripts.ui.diagram.DiamondUiElement;
import com.premature.floscript.scripts.ui.diagram.LogicBlockUiElement;
import com.premature.floscript.scripts.ui.diagram.OnDiagramEditorListener;
import com.premature.floscript.util.FloBus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.view.View.LAYER_TYPE_SOFTWARE;
import static com.premature.floscript.scripts.ui.diagram.DiagramValidator.DiagramValidationEvent;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScriptingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class ScriptingFragment extends Fragment implements SaveDialog.OnSaveDialogListener,
        LoadDialog.OnLoadDialogListener {
    private static final String TAG = "SCRIPT_FRAG";


    private LogicBlockUiElement mLogicBlockElement;
    private DiamondUiElement mDiamondElement;
    private ArrowUiElement mArrowElement;


    @InjectView(R.id.preview)
    ImageView preview;

    @InjectView(R.id.script_editor)
    DiagramEditorView mDiagramEditorView;
    @InjectView(R.id.logic_elem_btn)
    Button mLogicElemBtn;
    @InjectView(R.id.diamond_elem_btn)
    Button mDiamondElemBtn;
    @InjectView(R.id.arrow_elem_btn)
    Button mArrowElemBtn;

    private float mDensity;
    private StickyButtonCoordinator mBtnCoordinator;
    private DiagramDao mDiagramDao;
    private DiagramToScriptCompiler mCompiler;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p/>
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

        setRetainInstance(true);
    }

    private void init() {
        this.mCompiler = new DiagramToScriptCompiler(getActivity());
        this.mDiagramDao = new DiagramDao(getActivity());
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mLogicBlockElement = new LogicBlockUiElement(null, (int) (40 * mDensity), (int) (40 * mDensity));
        this.mDiamondElement = new DiamondUiElement(null, (int) (40 * mDensity), (int) (40 * mDensity));
        this.mArrowElement = new ArrowUiElement(null, (int) (40 * mDensity), (int) (6 * mDensity));

        // align the elements inside the buttons
        mArrowElement.advanceBy(8, 10);
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
        switch (id) {
            case R.id.action_save:
                saveDiagram();
                return true;
            case R.id.action_load:
                loadDiagram();
                return true;
            case R.id.action_clear:
                clearEditor();
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
            case R.id.action_thumb:
                Log.d(TAG, "preview");
                thumbnail();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearEditor() {
        this.mDiagramEditorView.setDiagram(Diagram.createEmptyDiagram());
    }

    private void thumbnail() {
        if (preview.getVisibility() == View.INVISIBLE) {
            preview.setVisibility(View.VISIBLE);
            preview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            preview.setImageDrawable(mDiagramEditorView.getDrawable());
        }
        else {
            preview.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe
    public void onDiagramValidationError(DiagramValidationEvent validationEvent) {
        TextPopupDialog.showPopup(getFragmentManager(), validationEvent.msg);
    }

    //TODO: get rid of adming stuff
    private void adminCode() {
        FloDbHelper mDb = FloDbHelper.getInstance(getActivity());
        mDb.wipe();
    }

    private void compileAndRunDiagram() {
        try {
            Script script = mCompiler.compile(mDiagramEditorView.getDiagram());
            String result = new ScriptEngine(getActivity().getApplicationContext()).runScript(script);
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
        if (!mDiagramEditorView.isDiagramValid()) {
            return;
        }
        SaveDialog dialog = new SaveDialog();
        dialog.setTargetFragment(this, 1);
        dialog.show(getActivity().getSupportFragmentManager(), "save dialog");
    }

    @Override
    public void loadClicked(String name) {
        new LoadTask(this).execute(name);
    }

    @Override
    public void saveClicked(String name, String description) {
        final Diagram diagram = mDiagramEditorView.getDiagram();
        diagram.setName(name);
        diagram.setDescription(description);
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
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_scripting, container, false);
        ButterKnife.inject(this, view);
        initButtons();

        FloBus.getInstance().register(this);
        mDiagramEditorView.busRegister(true);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloBus.getInstance().unregister(this);
        mDiagramEditorView.busRegister(false);
    }

    private void initButtons() {
        mBtnCoordinator = new StickyButtonCoordinator();
        mDiagramEditorView.setOnDiagramEditorListener(mBtnCoordinator);

        mLogicElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mLogicElemBtn.setCompoundDrawables(mLogicBlockElement.getDrawable(), null, null, null);
        mLogicElemBtn.setPadding(8, 0, 0, 0);
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
        mDiamondElemBtn.setPadding(8, 0, 0, 0);
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
     * This class turns our buttons into stateful push buttons.
     * </p>
     * This means that they can no longer register click events, because this class consumes all touch
     * events. For that reason, a user of this class who desires custom behaviour for onDiagramMenuItemClick events
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
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBtnCoordinator.unpressOtherButtons(this);
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
    private static class StickyButtonCoordinator implements OnDiagramEditorListener {
        private List<StickyButtonOnTouchListener> mElementButtons = new ArrayList<>();
        private StickyButtonOnTouchListener mPinUnpinListener;
        private Button mPinUButton;

        @Override
        public void onElementSelected(DiagramElement element) {
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

        public void unpressOtherButtons(StickyButtonOnTouchListener stickyButtonOnTouchListener) {
            for (StickyButtonOnTouchListener listener : mElementButtons) {
                if (stickyButtonOnTouchListener != listener && listener.isPressed()) {
                    listener.doOnClick();
                    listener.setPressed(false);
                }
            }
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
            } else {
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
            } else {
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
