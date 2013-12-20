package com.pmease.gitop.web.page.admin;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.setting.SystemSetting;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
public class SystemSettingEdit extends AbstractLayoutPage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		SystemSetting systemSetting = Gitop.getInstance(ConfigManager.class).getSystemSetting();

		final EditContext editContext = EditableUtils.getContext(systemSetting);
		
		Form<?> form = new Form<Void>("form"){

			@Override
			protected void onSubmit() {
				editContext.validate();
				if (!editContext.hasValidationError()) {
					Gitop.getInstance(ConfigManager.class).saveSystemSetting((SystemSetting) editContext.getBean());
					getSession().info("System setting has been updated.");
					setResponsePage(SystemSettingEdit.class);
				}
			}
			
		}; 
		form.add((Component)editContext.renderForEdit("objectEditor"));
		
		add(form);
	}

	@Override
	protected String getPageTitle() {
		return "System Setting";
	}
	
}
