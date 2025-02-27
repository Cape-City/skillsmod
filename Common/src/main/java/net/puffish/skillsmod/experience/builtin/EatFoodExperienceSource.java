package net.puffish.skillsmod.experience.builtin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.experience.calculation.condition.ItemCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemNbtCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemTagCondition;
import net.puffish.skillsmod.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.Map;

public class EatFoodExperienceSource implements ExperienceSource {

	public static final Identifier ID = SkillsMod.createIdentifier("eat_food");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("item", ConditionFactory.map(ItemCondition::parse, Context::item)),
			Map.entry("item_nbt", ConditionFactory.map(ItemNbtCondition::parse, Context::item)),
			Map.entry("item_tag", ConditionFactory.map(ItemTagCondition::parse, Context::item))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", ParameterFactory.map(EffectParameter::parse, Context::player)),
			Map.entry("food_hunger", ParameterFactory.simple(Context::hunger)),
			Map.entry("food_saturation", ParameterFactory.simple(Context::saturation))
	);

	private final CalculationManager<Context> manager;

	private EatFoodExperienceSource(CalculationManager<Context> calculated) {
		this.manager = calculated;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				maybeDataElement -> maybeDataElement
						.andThen(JsonElementWrapper::getAsObject)
						.andThen(EatFoodExperienceSource::create)
		);
	}

	private static Result<EatFoodExperienceSource, Error> create(JsonObjectWrapper rootObject) {
		return CalculationManager.create(rootObject, CONDITIONS, PARAMETERS).mapSuccess(EatFoodExperienceSource::new);
	}

	private record Context(ServerPlayerEntity player, ItemStack item) {
		public double hunger() {
			var fc = item.getItem().getFoodComponent();
			return fc == null ? 0.0 : fc.getHunger();
		}
		public double saturation() {
			var fc = item.getItem().getFoodComponent();
			return fc == null ? 0.0 : fc.getSaturationModifier();
		}
	}

	public int getValue(ServerPlayerEntity player, ItemStack itemStack) {
		return manager.getValue(new Context(player, itemStack));
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
