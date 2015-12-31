//////////////////////////////////////////////////////////////////////////////
//	Copyright 2011 Alex Leffelman
//	
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//	
//	http://www.apache.org/licenses/LICENSE-2.0
//	
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//////////////////////////////////////////////////////////////////////////////

package com.clogic.karaokeviewer.Midi.event.meta;

import com.clogic.karaokeviewer.Util.Logger;

import java.io.UnsupportedEncodingException;

public class Lyrics extends TextualMetaEvent
{
    public Lyrics(long tick, long delta, String lyric)
    {
        super(tick, delta, MetaEvent.LYRICS, lyric);
    }

    public void setLyric(String t)
    {
        setText(t);
    }

    public String getLyric()
    {
        return getText();
    }

    @Override
    public String toString() {
            return this.getClass().getSimpleName() + ", " + mText.length();
    }
}
