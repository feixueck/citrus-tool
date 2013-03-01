package com.alibaba.ide.plugin.eclipse.springext.hyperlink.detector;

import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;

import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

/**
 * 这是一个hyperlink detector的基类，方便取得context相关的对象。
 * <p/>
 * 在初始化的时候，此类要求传入一个<code>IAdaptable</code>
 * 对象作为context，可以从中取得诸如project、schemas等对象。 这个context一般是通过
 * <code>TextViewerConfiguration</code>对象传入的。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractContextualHyperlinkDetector extends AbstractHyperlinkDetector {
    protected final <T> T getFromContext(Class<T> type) {
        return SpringExtPluginUtil.getFromContext(this, type, true);
    }
}
