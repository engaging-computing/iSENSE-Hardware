package edu.uml.cs.isense.gcollector.shared;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import edu.uml.cs.isense.gcollector.objects.DataSet;

public class QueueParentAssets {
	public Queue<DataSet> uploadQueue;
	public Queue<DataSet> mirrorQueue;
	public String parentName;
	public Context mContext;

	QueueParentAssets(Queue<DataSet> queue, String parentName, Context context) {
		this.uploadQueue = queue;
		this.parentName = parentName;
		this.mContext = context;
		this.mirrorQueue = new LinkedList<DataSet>();
		mirrorQueue.addAll(queue);
	}
}
