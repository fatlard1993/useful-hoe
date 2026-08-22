package com.justfatlard.usefulhoe.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import justfatlard.village_quests.api.LessonApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * A farmer teaching what a hoe does once this mod is installed.
 *
 * <p>Registered with Village Quests when that mod is present. Nearly everything
 * here is invisible from inside the game: that the whole mod is gated behind an
 * enchantment, that the off-hand picks which job a click does, that a hoe can
 * carry Fortune and that it now means something, and what a wide swing costs
 * the tool.
 *
 * <p>Gains a sixth lesson when {@code emerald-tools} is installed, because that
 * mod answers the question the fifth one raises. The emerald hoe is reached by
 * registry id rather than by compiling against it: one item is not worth a
 * second soft dependency.
 *
 * <p>Area sizes and durability costs quoted below are this mod's defaults, from
 * {@code ModConfig}. They are configurable, so the dialogue says "usually".
 *
 * <p>This class must only be touched behind a mod-loaded check. It refers to
 * Village Quests types directly, so loading it without that mod present throws.
 */
public final class HoeQuestRegistration {
	private HoeQuestRegistration() {}

	private static final Identifier REACH = Identifier.fromNamespaceAndPath("useful-hoe", "reach");
	private static final Identifier EMERALD_HOE =
		Identifier.fromNamespaceAndPath("emerald-tools-justfatlard", "emerald_hoe");

	private static final boolean EMERALD_TOOLS = FabricLoader.getInstance().isModLoaded("emerald-tools-justfatlard");

	/** Resolved on every call: registries are not filled when this craft registers. */
	private static Item emeraldHoe() {
		return BuiltInRegistries.ITEM.getOptional(EMERALD_HOE).orElse(null);
	}

	private static boolean isHoe(ItemStack stack) {
		Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return id != null && id.getPath().endsWith("_hoe");
	}

	private static boolean hasReach(ItemStack stack) {
		ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
		if (enchantments == null) return false;
		for (Holder<Enchantment> held : enchantments.keySet()) {
			if (held.is(REACH)) return true;
		}
		return false;
	}

	private static Predicate<ItemStack> atLeast(Item item, int count) {
		return stack -> stack.is(item) && stack.getCount() >= count;
	}

	public static void register() {
		LessonApi.register(new LessonApi.Craft(
			"useful-hoe:farming",
			"farmer",
			LessonApi.Policy.standard(),
			lessons(),
			new LessonApi.Openings(
				LessonApi.lines(
					"{former} is gone. They had you part-way through and the field does not care, but I do. ",
					"You were learning off {former}, weren't you. I'd not have said a word while they were working. Since they aren't: ",
					"*leans on the fence* {former}'s student. I know roughly where they had got you. "),
				LessonApi.lines(
					"There's another when you want it. Field's not going anywhere.",
					"*wipes hands* Next one whenever. Best done in the morning, mind.",
					"One more waiting on you. No rush -- everything here takes the time it takes."),
				LessonApi.lines(
					"{former} is gone. Their hoe is still against the shed wall where they left it.",
					"You'll have heard about {former}. They were teaching you the wide swing, weren't they."),
				LessonApi.lines(
					"*watches you sneak past the seedlings* You knew to hold that. Most people learn it by ruining a row.",
					"You've got a hoe on you and it's worn honest. That's a working tool, not a carried one.",
					"{mentor} taught you. Thought as much -- they always start people on the enchantment.")),
			new LessonApi.Hooks() {
				@Override
				public void onGraduate(ServerPlayer player, ServerLevel world, LessonApi.Teacher teacher) {
					teacher.give(new ItemStack(Items.BONE_MEAL, 16));
					teacher.says("Take the meal. You'll use it faster than you think now.");
					teacher.laterInTheVillage("The east field has been turned end to end and nobody saw it being done. "
						+ "There is one square by the gate left rough, where somebody stopped to think.", 0);
				}
			}));
	}

	private static List<LessonApi.Lesson> lessons() {
		List<LessonApi.Lesson> lessons = new ArrayList<>(List.of(
			new LessonApi.Lesson(
				"You have been farming a square at a time like your grandfather. Bring me a hoe with Reach on it -- any hoe, any level, "
					+ "off a table or a book, I do not mind. Until it has that word on it you are holding a stick with an edge.",
				"bring {name} a hoe enchanted with Reach",
				"Without that word on it, a hoe turns one square. That is the whole difference.",
				"*turns it over, finds the enchantment* There. Understand that this is the entire thing -- a hoe with nothing on it "
					+ "works exactly as it always has, one square at a time. Reach is what makes it a farming tool instead of a "
					+ "ground-breaking tool.",
				"Five levels. The first gives you a strip of three, and the fifth gives you nine across and eighteen deep, which is more "
					+ "field than you think until you are standing in it. And nothing but a hoe will take it, so do not waste the table "
					+ "on your boots.",
				Items.DIAMOND_HOE, stack -> isHoe(stack) && hasReach(stack), 6),

			new LessonApi.Lesson(
				"Sixteen bone meal. Do not ask me what for yet -- I want it in your hand while I tell you where to put it, because "
					+ "everyone puts it in the wrong one.",
				"bring {name} sixteen bone meal",
				"Off-hand. The hand you are not swinging with is the one that decides.",
				"*takes it, then points at your other hand* That hand. Bone meal in the off-hand and one click feeds every growing "
					+ "thing in the area. Seeds in that hand instead and the same click plants them. Empty, and it just breaks ground.",
				"And it does one job per swing, in order -- break the ground, then plant it, then feed it, then take it in. So the same "
					+ "click on the same square does something different depending on what the field needs and what you are holding. "
					+ "Four swings works a field from grass to harvest.",
				Items.BONE_MEAL, atLeast(Items.BONE_MEAL, 16), 6),

			new LessonApi.Lesson(
				"Thirty-two wheat. And when you cut it, watch the ground behind you rather than the crop in your hand.",
				"bring {name} thirty-two wheat",
				"It puts the seed back as it takes the crop. You never replant a row again.",
				"*sets it aside without counting* You saw it. It replants behind you -- takes the wheat, puts the seed back in the "
					+ "worked ground, and moves on. That is the single largest hour of the farming year, gone.",
				"And here is the one nobody works out: Fortune. On a hoe. It has always been allowed and it has never meant anything, "
					+ "because a hoe never harvested anything before. Put Fortune on the hoe you swing and the field gives more every "
					+ "time you cut it.",
				Items.WHEAT, atLeast(Items.WHEAT, 32), 8),

			new LessonApi.Lesson(
				"Eight sweet berries. Off a bush -- and I want the bush still standing when you are done, which if you use the hoe it "
					+ "will be.",
				"bring {name} eight sweet berries",
				"The bush is still standing. It picks rather than clears.",
				"*checks them over* No leaves in with them. Good -- the hoe picks a berry bush instead of tearing it out, which is more "
					+ "than can be said for how most people harvest them.",
				"Same manners with anything that grows in a column. Cane, bamboo, cactus, kelp -- one swing takes everything above the "
					+ "root and leaves the root in the ground. You never have to work out where to cut, which is the part people get "
					+ "wrong and then wonder why the cane stopped coming.",
				Items.SWEET_BERRIES, atLeast(Items.SWEET_BERRIES, 8), 8),

			new LessonApi.Lesson(
				"Bring me a hoe you have actually worn down. Not a new one -- I want to see the damage on it. If you have been swinging "
					+ "wide you will not have to go looking.",
				"bring {name} a worn hoe",
				"One for the swing, and one for every block it touched.",
				"*thumbs the edge* There it is. That is what the wide swing costs: one off the tool for the swing, and one more for "
					+ "every single block it reached. Usually. A full nine by eighteen over open ground is most of two hundred, in one "
					+ "click, and people cannot understand why their good hoe died in a week.",
				"So hold sneak when you want one square and mean it -- that puts you back to the old way on purpose. And think harder "
					+ "than you have been about what the hoe is made of. A tool you cannot keep sharp is a tool you stop reaching for.",
				Items.DIAMOND_HOE, stack -> isHoe(stack) && stack.isDamaged(), 10)));

		if (EMERALD_TOOLS) lessons.add(emeraldLesson());
		return List.copyOf(lessons);
	}

	/**
	 * The answer to the durability lesson, when the mod that provides it is here.
	 *
	 * <p>The claim is checkable: emerald's enchantability is 20 against
	 * netherite's 15 and diamond's 10, and gold's 22 comes with 32 durability.
	 * For a hoe that is the stat that matters, because the whole mod is behind
	 * an enchantment and Reach V is an expensive roll.
	 */
	private static LessonApi.Lesson emeraldLesson() {
		return new LessonApi.Lesson(
			"One last thing, and it is the one I would have told you first if you would have listened. Bring me an emerald hoe. Yes, "
				+ "emerald. I know what it looks like on the rack next to netherite. Bring it and I will tell you why the rack is wrong.",
			"bring {name} an emerald hoe",
			"It takes an enchantment better than netherite does. That is the whole reason.",
			"*weighs it, unimpressed on purpose* Lasts longer than diamond and not as long as netherite, and neither of those is why "
				+ "you want it. It takes an enchantment better than either -- better than anything you would actually carry. Only gold "
				+ "does better and a gold hoe is gone in an afternoon.",
			"Which is the answer to everything I have told you. The whole mod is behind one enchantment and the top level of it is a "
				+ "dear roll -- a tool that takes enchantment well is a tool that will actually offer you the wide swing. -- And it is "
				+ "made of the stuff. Turn enough ground with it and now and again it hands you one back out of the soil. Not often. "
				+ "I have had three in my life and I remember all three.",
			null, stack -> {
				Item hoe = emeraldHoe();
				return hoe != null && stack.is(hoe);
			}, 12);
	}
}
