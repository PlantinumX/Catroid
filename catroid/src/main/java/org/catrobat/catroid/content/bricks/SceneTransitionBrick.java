/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.content.bricks;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.view.View;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Nameable;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.content.bricks.brickspinner.BrickSpinner;
import org.catrobat.catroid.content.bricks.brickspinner.NewOption;
import org.catrobat.catroid.ui.recyclerview.dialog.NewSceneDialogFragment;
import org.catrobat.catroid.ui.recyclerview.dialog.dialoginterface.NewItemInterface;

import java.util.ArrayList;
import java.util.List;

public class SceneTransitionBrick extends BrickBaseType implements NewItemInterface<Scene>,
		BrickSpinner.OnItemSelectedListener<Scene> {

	private static final long serialVersionUID = 1L;

	private String sceneForTransition;

	private transient BrickSpinner<Scene> spinner;

	public SceneTransitionBrick(String scene) {
		this.sceneForTransition = scene;
	}

	public String getSceneForTransition() {
		return sceneForTransition;
	}

	public void setSceneForTransition(String sceneForTransition) {
		this.sceneForTransition = sceneForTransition;
	}

	@Override
	public BrickBaseType clone() throws CloneNotSupportedException {
		SceneTransitionBrick clone = (SceneTransitionBrick) super.clone();
		clone.spinner = null;
		return clone;
	}

	@Override
	public int getViewResource() {
		return R.layout.brick_scene_transition;
	}

	@Override
	public View getPrototypeView(Context context) {
		super.getPrototypeView(context);
		return getView(context);
	}

	@Override
	public View getView(final Context context) {
		super.getView(context);

		List<Nameable> items = new ArrayList<>();
		items.add(new NewOption(context.getString(R.string.new_option)));
		items.addAll(ProjectManager.getInstance().getCurrentProject().getSceneList());
		items.remove(ProjectManager.getInstance().getCurrentlyEditedScene());
		spinner = new BrickSpinner<>(R.id.brick_scene_transition_spinner, view, items);
		spinner.setOnItemSelectedListener(this);
		spinner.setSelection(sceneForTransition);

		return view;
	}

	@Override
	public void onNewOptionSelected() {
		new NewSceneDialogFragment(this, ProjectManager.getInstance().getCurrentProject()) {

			@Override
			public void onCancel(DialogInterface dialog) {
				super.onCancel(dialog);
				spinner.setSelection(sceneForTransition);
			}
		}.show(((Activity) view.getContext()).getFragmentManager(), NewSceneDialogFragment.TAG);
	}

	@Override
	public void addItem(Scene item) {
		ProjectManager.getInstance().getCurrentProject().addScene(item);
		spinner.add(item);
		spinner.setSelection(item);
	}

	@Override
	public void onStringOptionSelected(String string) {
	}

	@Override
	public void onItemSelected(@Nullable Scene item) {
		sceneForTransition = item != null ? item.getName() : null;
	}

	@Override
	public List<ScriptSequenceAction> addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
		sequence.addAction(sprite.getActionFactory().createSceneTransitionAction(sceneForTransition));
		return null;
	}
}
