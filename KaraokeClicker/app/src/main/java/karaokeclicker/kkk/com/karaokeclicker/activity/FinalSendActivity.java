package karaokeclicker.kkk.com.karaokeclicker.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import karaokeclicker.kkk.com.karaokeclicker.R;

public class FinalSendActivity extends Activity {

    @Bind(R.id.edit_phone)
    EditText editPhone;

    @Bind(R.id.checkbox_agree)
    CheckBox checkBox;

    @Bind(R.id.btn_send)
    Button btnSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_send);

        ButterKnife.bind(this);

        initButton();
    }

    private void initButton() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCellphone(editPhone.getText().toString()) && editPhone.getText().toString().length() != 0) {
                    if (checkBox.isChecked()) {
                        Toast.makeText(FinalSendActivity.this, "서버가 미구현되여 초기화면으로 이동합니다", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(FinalSendActivity.this, "약관에 동의해주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FinalSendActivity.this, "전화번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public boolean isCellphone(String str) {
        return str.matches("(01[016789])(\\d{3,4})(\\d{4})");
    }

}
