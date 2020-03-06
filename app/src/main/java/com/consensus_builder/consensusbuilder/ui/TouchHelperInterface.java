package com.consensus_builder.consensusbuilder.ui;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

/**
 * This interface is used to provide callbacks for using RecyclerViews.
 * Why use this? it greatly simplifies the process of dragging and swiping
 * items (Cards) within said RecyclerViews.<br>
 * <br>
 * Implementers of this Interface must provide callbacks for whatever may be listening
 * for move or dismissal events from a
 * {@link android.support.v7.widget.helper.ItemTouchHelper.Callback}.<br>
 * <br>
 * For example, an Adapter connected to a RecyclerView has to inherit from
 * {@link RecyclerView.Adapter}. By implementing <code>TouchHelperInterface</code>
 * it can receive <code>onItemMove()</code> and <code>onItemDismiss()</code> calls when
 * the user moves or dismisses an item in the RecyclerView.
 *
 * The link between the RecyclerView, its Adapter and the callback is made
 * when a {@link android.support.v7.widget.helper.ItemTouchHelper} attaches
 * to the RecyclerView.
 * For an example, see <code>QuestionnaireFragment.onActivityCreated(Bundle)</code>.
 */
public interface TouchHelperInterface {

    /**
     * Called when an item has been dragged far enough to trigger a move.<br>
     * <br>
     * <b>NOTE</b> that this is called EVERY TIME this item has shifted,
     * NOT at the end of it's "drop" event.
     *
     * @param viewHolder    The ViewHolder for the View that was moved.  May be
     *                      useful if you want to indicated changes ON that View.
     *
     * @param targetViewHolder  The ViewHolder that the first param is moved OVER.
     *
     * @param fromPos       The start position of the item moved.
     *
     * @param toPos         Position after this shift.
     */
    void onItemMove (RecyclerView.ViewHolder viewHolder,
                     RecyclerView.ViewHolder targetViewHolder,
                     int fromPos, int toPos);

    /**
     * Called when an item has been dismissed with a swipe><br>
     * <br>
     * Implementations should call {@link android.support.v7.widget.RecyclerView.Adapter#notifyItemRemoved(int)}
     * after adjusting the underlying data to reflect this dismissal.
     *
     * @param viewHolder    The ViewHolder that is being dismissed.
     *
     * @param pos           The position of the item dismissed.
     *
     * @see RecyclerView#getAdapterPositionFor(RecyclerView.ViewHolder)
     *
     * @see RecyclerView.ViewHolder#getAdapterPosition()
     */
    void onItemDismiss (RecyclerView.ViewHolder viewHolder, int pos);

}
