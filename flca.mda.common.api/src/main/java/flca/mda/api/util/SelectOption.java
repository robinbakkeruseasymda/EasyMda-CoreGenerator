package flca.mda.api.util;

import java.util.ArrayList;
import java.util.List;

public enum SelectOption
{
	EXCLUDE_ID,   // By default the Val Id will be include
	EXCLUDE_VAL,
	EXCLUDE_VAR,
	EXCLUDE_ONETOMANY,
	EXCLUDE_MANYTOMANY,
	EXCLUDE_MANYTOONE,
	EXCLUDE_ONETOONE,
	EXCLUDE_RELATIONS,
	EXCLUDE_DISCRIMINATOR,
	EXCLUDE_OFD;
	
	/**
	 * Eventually add SelectOption to input array of SelectOption's
	 * @param opts
	 * @param addOpt
	 * @return
	 */
	public static SelectOption[] addOption(SelectOption[] opts,
			SelectOption... addOpts) {

		SelectOption[] result = opts; 
		List<SelectOption> list = new ArrayList<>();
		for (SelectOption opt : opts) {
			list.add(opt);
		}
		
		for (SelectOption opt : addOpts) {
			if (!list.contains(opt)) {
				list.add(opt);
			} 
		}

		result = new SelectOption[list.size()];
		list.toArray(result);

		return result;
		
	}
	
}
