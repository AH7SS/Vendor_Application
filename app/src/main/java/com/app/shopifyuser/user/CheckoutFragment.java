package com.app.shopifyuser.user;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.app.shopifyuser.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CheckoutFragment extends DialogFragment implements View.OnClickListener {

    private TextView deliveryDateSetterTv, deliveryTimeSetterTv;
    private Button confirmScheduleBtn, orderNowBtn;


    private final Integer[] meetingStartTime = new Integer[5];
    private long scheduleTime;
    private boolean timeWasSelected, dateWasSelected;
    private ScheduleDeliveryListener scheduleDeliveryListener;

    public interface ScheduleDeliveryListener {
        void confirmSchedule(long time);
    }

    public CheckoutFragment(ScheduleDeliveryListener scheduleDeliveryListener) {
        this.scheduleDeliveryListener = scheduleDeliveryListener;
    }

    public CheckoutFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.checkout_dialog_background);
        }


        deliveryDateSetterTv = view.findViewById(R.id.deliveryDateSetterTv);
        deliveryTimeSetterTv = view.findViewById(R.id.deliveryTimeSetterTv);
        confirmScheduleBtn = view.findViewById(R.id.confirmScheduleBtn);
        orderNowBtn = view.findViewById(R.id.orderNowBtn);


        deliveryDateSetterTv.setOnClickListener(this);
        deliveryTimeSetterTv.setOnClickListener(this);
        confirmScheduleBtn.setOnClickListener(this);
        orderNowBtn.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == deliveryDateSetterTv.getId()) {

            getMeetingDate();

        } else if (v.getId() == deliveryTimeSetterTv.getId()) {

            getMeetingTime();

        } else if (v.getId() == confirmScheduleBtn.getId()) {

            if (timeWasSelected && dateWasSelected) {
                dismiss();
                scheduleDeliveryListener.confirmSchedule(scheduleTime);
            }



        } else if (v.getId() == orderNowBtn.getId()) {

            scheduleDeliveryListener.confirmSchedule(System.currentTimeMillis());

        }

    }


    private void getMeetingDate() {

        final Calendar mcurrentDate = Calendar.getInstance(Locale.getDefault());

        DatePickerDialog StartTime = new DatePickerDialog(getContext(),
                (view, year, monthOfYear, dayOfMonth) -> {

                    if (mcurrentDate.get(Calendar.YEAR) > year ||

                            (mcurrentDate.get(Calendar.YEAR) == year &&
                                    mcurrentDate.get(Calendar.MONTH) > monthOfYear) ||

                            (mcurrentDate.get(Calendar.YEAR) == year &&
                                    mcurrentDate.get(Calendar.MONTH) == monthOfYear &&
                                    mcurrentDate.get(Calendar.DAY_OF_MONTH) > dayOfMonth)
                    ) {

                        String text;
                        if ((timeWasSelected &&
                                mcurrentDate.get(Calendar.YEAR) == year &&
                                mcurrentDate.get(Calendar.MONTH) == monthOfYear &&
                                mcurrentDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth &&
                                (meetingStartTime[3] < mcurrentDate.get(Calendar.HOUR))
                                && meetingStartTime[4] < mcurrentDate.get(Calendar.MINUTE))) {

                            text = "Cannot " +
                                    "Please Selected a different time day or change the day";

                        } else {
                            text = "Course time cannot be scheduled to this time!";
                        }
                        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                    } else {

                        dateWasSelected = true;
                        meetingStartTime[0] = year;
                        meetingStartTime[1] = monthOfYear;
                        meetingStartTime[2] = dayOfMonth;
                        deliveryDateSetterTv.setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);

                        if (timeWasSelected) {
                            calculateTime();
                        }
                    }

                }, mcurrentDate.get(Calendar.YEAR), mcurrentDate.get(Calendar.MONTH),
                mcurrentDate.get(Calendar.DAY_OF_MONTH));
        StartTime.show();

    }


    private void getMeetingTime() {

        final Calendar mcurrentTime = Calendar.getInstance(Locale.getDefault());

        final TimePickerDialog mTimePicker = new TimePickerDialog(
                getContext(), (timePicker, selectedHour, selectedMinute) -> {

            final Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTime(new Date(System.currentTimeMillis()));

            calendar.set(Calendar.HOUR, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);


            if (calendar.getTimeInMillis() >= calendar.getTimeInMillis()) {

                timeWasSelected = true;
                meetingStartTime[3] = selectedHour;
                meetingStartTime[4] = selectedMinute;
                deliveryTimeSetterTv.setText(selectedHour + ":" + selectedMinute);

                if (dateWasSelected) {
                    calculateTime();
                }
            } else {
                Toast.makeText(getContext(),
                        "order can't be at selected time!", Toast.LENGTH_SHORT).show();
            }

        }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE),
                true);
        mTimePicker.setTitle("Select order Time");
        mTimePicker.show();

    }

    private void calculateTime() {

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(meetingStartTime[0], meetingStartTime[1], meetingStartTime[2],
                meetingStartTime[3], meetingStartTime[4]);

        scheduleTime = calendar.getTimeInMillis();

        confirmScheduleBtn.setBackgroundResource(R.drawable.btn_background);
        Log.d("ttt", "scheduleTime: " + scheduleTime);

    }


}
