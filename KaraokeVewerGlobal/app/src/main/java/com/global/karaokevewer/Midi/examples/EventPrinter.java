package com.global.karaokevewer.Midi.examples;

import com.global.karaokevewer.Midi.MidiFile;
import com.global.karaokevewer.Midi.event.MidiEvent;
import com.global.karaokevewer.Midi.event.NoteOn;
import com.global.karaokevewer.Midi.event.meta.Tempo;
import com.global.karaokevewer.Midi.util.MidiEventListener;
import com.global.karaokevewer.Midi.util.MidiProcessor;

import java.io.File;
import java.io.IOException;

public class EventPrinter implements MidiEventListener
{
    private String mLabel;

    public EventPrinter(String label)
    {
        mLabel = label;
    }

    // 0. Implement the listener functions that will be called by the
    // MidiProcessor
    @Override
    public void onStart(boolean fromBeginning)
    {
        if(fromBeginning)
        {
            System.out.println(mLabel + " Started!");
        }
        else
        {
            System.out.println(mLabel + " resumed");
        }
    }

    @Override
    public void onEvent(MidiEvent event, long ms)
    {
        System.out.println(mLabel + " received event: " + event);
    }

    @Override
    public void onStop(boolean finished)
    {
        if(finished)
        {
            System.out.println(mLabel + " Finished!");
        }
        else
        {
            System.out.println(mLabel + " paused");
        }
    }

    public static void main(String[] args)
    {
        // 1. Read in a MidiFile
        MidiFile midi = null;
        try
        {
            midi = new MidiFile(new File("inputmid.mid"));
        }
        catch(IOException e)
        {
            System.err.println(e);
            return;
        }

        // 2. Create a MidiProcessor
        MidiProcessor processor = new MidiProcessor(midi);

        // 3. Register listeners for the events you're interested in
        EventPrinter ep = new EventPrinter("Individual Listener");
        processor.registerEventListener(ep, Tempo.class);
        processor.registerEventListener(ep, NoteOn.class);

        // or listen for all events:
        EventPrinter ep2 = new EventPrinter("Listener For All");
        processor.registerEventListener(ep2, MidiEvent.class);

        // 4. Start the processor
        processor.start();

        // Listeners will be triggered in real time with the MIDI events
        // And you can pause/resume with stop() and start()
        try
        {
            Thread.sleep(10 * 1000);
            processor.stop();

            Thread.sleep(10 * 1000);
            processor.start();
        }
        catch(Exception e)
        {
        }
    }
}
