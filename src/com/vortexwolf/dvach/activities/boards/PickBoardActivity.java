package com.vortexwolf.dvach.activities.boards;


import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.presentation.services.Tracker;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class PickBoardActivity extends ListActivity {

	public static final String TAG = "PickBoardActivity";
	
    public static final IBoardListEntity[] BOARDS = {
    	new SectionEntity("Разное"),
    	new BoardEntity("b", "бред"),
    	new BoardEntity("fag", "фагготрия"),
    	new BoardEntity("soc", "общение"),
    	new BoardEntity("r", "просьбы"),
    	new BoardEntity("int", "international"),
    	
    	new SectionEntity("Тематика"),
    	new BoardEntity("au", "автомобили и транспорт"),
    	new BoardEntity("bi", "велосипеды"),
    	new BoardEntity("biz", "бизнес"),
    	new BoardEntity("bo", "книги"),
    	new BoardEntity("c", "комиксы и мультфильмы"),
    	new BoardEntity("di", "столовая"),
    	new BoardEntity("em", "другие страны и туризм"),
    	new BoardEntity("ew", "выживание и самооборона"),
    	new BoardEntity("fa", "мода и стиль"),
    	new BoardEntity("fiz", "физкультура"),
    	new BoardEntity("fl", "иностранные языки"),
    	new BoardEntity("gd", "gamedev"),
    	new BoardEntity("hi", "история"),
    	new BoardEntity("hw", "железо"),
    	new BoardEntity("me", "медицина"),
    	new BoardEntity("mg", "магия"),
    	new BoardEntity("mlp", "my little pony"),
    	new BoardEntity("mo", "мотоциклы"),
    	new BoardEntity("mu", "музыка"),
    	new BoardEntity("ne", "животные и природа"),
    	new BoardEntity("po", "политика и новости"),
    	new BoardEntity("pr", "программирование"),
    	new BoardEntity("psy", "психология"),
    	new BoardEntity("ra", "радиотехника"),
    	new BoardEntity("re", "религия и философия"),
    	new BoardEntity("s", "программы"),
    	new BoardEntity("sf", "научная фантастика"),
    	new BoardEntity("sci", "наука"),
    	new BoardEntity("sn", "паранормальные явления"),
    	new BoardEntity("sp", "спорт"),
    	new BoardEntity("spc", "космос"),
    	new BoardEntity("t", "технологии"),
    	new BoardEntity("tv", "тв и кино"),
    	new BoardEntity("un", "образование"),
    	new BoardEntity("wh", "warhammer"),
    	new BoardEntity("wm", "военная техника"),
    	new BoardEntity("w", "оружие"),
    	
    	new SectionEntity("Творчество"),
    	new BoardEntity("de", "дизайн"),
		new BoardEntity("diy", "хобби"),
		new BoardEntity("f", "flash & gif"),
		new BoardEntity("pa", "живопись"),
		new BoardEntity("p", "фото"),
		new BoardEntity("wp", "обои и высокое разрешение"),
        new BoardEntity("td", "трёхмерная графика"),
        
    	new SectionEntity("Игры"),
    	new BoardEntity("bg", "настольные игры"),
		new BoardEntity("gb", "азартные игры"),
		new BoardEntity("mc", "minecraft"),
		new BoardEntity("mmo", "MMO"),
		new BoardEntity("vg", "видеоигры"),
		new BoardEntity("wr", "ролевые игры"),
		new BoardEntity("tes", "the elder scrolls"),
		
		new SectionEntity("Японская культура"),
		new BoardEntity("a", "аниме"),
		new BoardEntity("aa", "аниме арт"),
		new BoardEntity("fd", "фэндом"),
		new BoardEntity("ma", "манга"),
		new BoardEntity("vn", "визуальные новеллы")
    };

    private MainApplication mApplication;
    private Tracker mTracker;
    private BoardsListAdapter mAdapter = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        this.mApplication = (MainApplication) this.getApplication();
        this.mTracker = this.mApplication.getTracker();
        
        this.mTracker.clearBoardVar();
        this.mTracker.trackActivityView(TAG);
        
        this.resetUI();
        
        this.mAdapter = new BoardsListAdapter(this, BOARDS);
        this.setListAdapter(this.mAdapter);
        
        this.setTitle(this.getString(R.string.pick_board_title));
    }

    private void resetUI()
    {
    	this.setTheme(this.mApplication.getSettings().getTheme());
    	this.setContentView(R.layout.pick_board_view);
        
        final Button pickBoardButton = (Button)findViewById(R.id.pick_board_button);
        final EditText pickBoardInput = (EditText)findViewById(R.id.pick_board_input);
        
        pickBoardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String enteredBoard = pickBoardInput.getText().toString().trim();
				returnBoard(enteredBoard);
			}
		});
        
        pickBoardInput.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		        	String enteredBoard = pickBoardInput.getText().toString().trim();
					returnBoard(enteredBoard);
		        	return true;
		        }
		        return false;
		    }
		});
        
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	BoardEntity item = (BoardEntity)this.mAdapter.getItem(position);
        returnBoard(item.getCode());
    }
    
    private void returnBoard(String boardCode) {
    	if(StringUtils.isEmpty(boardCode)){
			AppearanceUtils.showToastMessage(this, getString(R.string.warning_enter_board));
			return;
    	}
    	
    	// remove the slash if it was entered
    	if(boardCode.charAt(0) == '/'){
    		boardCode = boardCode.substring(1);
    	}

       	Intent intent = new Intent();
       	intent.putExtra(Constants.EXTRA_SELECTED_BOARD, boardCode);
       	setResult(RESULT_OK, intent);
       	finish();	
    }
    
}
