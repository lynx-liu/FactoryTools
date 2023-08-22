package com.android.factorytest.view;

import com.android.factorytest.R;
import com.android.factorytest.bean.TestItem;
import com.android.factorytest.utils.ImageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestResultView extends LinearLayout {

	private TestItem testItem;
	private ImageView iv_icon;
	private TextView tv_label;
	private Boolean setResult = null;
	Bitmap successbitmap;
	Bitmap failbitmap;

	public TestResultView(Context context, TestItem testItem) {
		super(context);

		this.testItem = testItem;
		LayoutInflater.from(context).inflate(R.layout.test_result_list_item,
				this);
		tv_label = (TextView) this.findViewById(R.id.tv_label);
		iv_icon = (ImageView) this.findViewById(R.id.iv_icon);
		initContent();
		successbitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.check_success);
		failbitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.check_fail);
	}

	public void setResult(Boolean result) {
		setResult = result;
		initContent();
		this.invalidate();
	}

	private void initContent() {
		tv_label.setText(testItem.label);
		Bitmap bitmap=null;
		if (setResult != null) {
			if (setResult) {
				 bitmap = ImageUtil.createBitmap(BitmapFactory.decodeResource(getResources(), testItem.iconId),successbitmap);
			}else{
				 bitmap = ImageUtil.createBitmap(BitmapFactory.decodeResource(getResources(), testItem.iconId),failbitmap);
			}
			iv_icon.setImageBitmap(bitmap);
		}else{
			iv_icon.setImageResource(testItem.iconId);
		}
	}
}
