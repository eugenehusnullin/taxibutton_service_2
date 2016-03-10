package tb.dao;

import java.util.List;

import tb.domain.Partner;
import tb.domain.PartnerSettings;

public interface IPartnerDao {

	Partner get(Long id);

	Partner get(String uuid);

	Partner getByApiId(String id);
	
	Partner getByApiId(String apiId, String apiKey);

	List<Partner> getAll();
	
	List<Partner> getActive();

	void save(Partner partner);
	
	void saveOrUpdate(Partner partner);
	
	void delete(Partner partner);

	void saveSettings(PartnerSettings settings);

	List<Partner> getPartnersByCodeName(String codeName);

	PartnerSettings getPartnerSettings(Partner partner);
}
