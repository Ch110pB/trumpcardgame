package org.alpacax.trumpcardgame;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.alpacax.trumpcarddb.Player;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Contract;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

@Controller
@AllArgsConstructor
@SessionAttributes({"gameDeck", "intFlags", "singlePlayer", "gameToken"})

public class GameController {

    private static final String GAME_DECK = "gameDeck";
    private static final String INT_FLAGS = "intFlags";
    private static final String SINGLE_PLAYER = "singlePlayer";
    private static final String GAME_TOKEN = "gameToken";
    private static final String REF_REDIRECT = "redirect:referee";
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Contract(pure = true)
    @GetMapping("/")
    public @NonNull String homepage() {

        return "homepage";
    }

    @GetMapping("/singlePlayerGame")
    public String initSinglePlayer(@NonNull RedirectAttributes attributes)
            throws SQLException, NoSuchAlgorithmException {

        Deck deck = GameHelper.getDecks();
        attributes.addFlashAttribute(GAME_DECK, deck);
        attributes.addFlashAttribute(INT_FLAGS, new FlagSet());
        attributes.addFlashAttribute(SINGLE_PLAYER, 1);
        attributes.addFlashAttribute(GAME_TOKEN, "null");

        return REF_REDIRECT;
    }

    @GetMapping("/new-game")
    public String initialize(@NonNull Model model)
            throws SQLException, NoSuchAlgorithmException {

        Deck deck = GameHelper.getDecks();
        String gameToken = GameHelper.createToken();
        while (!GameHelper.isTokenUnique(gameToken)) {
            gameToken = GameHelper.createToken();
        }
        GameHelper.updateSessionTable(new GameSession(gameToken, 0,
                deck, new FlagSet(), 1));
        model.addAttribute(GAME_TOKEN, gameToken);

        return "playerwait";
    }

    @PostMapping("/new-game")
    public String startGame(@RequestParam("token") String token,
                            @NonNull RedirectAttributes attributes)
            throws SQLException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String response = GameHelper.getSession(token);
        Deck deck = mapper.readValue(response, Deck.class);
        FlagSet flagSet = new FlagSet();
        flagSet.setTurnOwner(0);
        attributes.addFlashAttribute(GAME_DECK, deck);
        attributes.addFlashAttribute(INT_FLAGS, flagSet);
        attributes.addFlashAttribute(SINGLE_PLAYER, 0);
        attributes.addFlashAttribute(GAME_TOKEN, token);

        return REF_REDIRECT;
    }

    @GetMapping("/referee")
    public String referee(@NonNull Model model,
                          SessionStatus sessionStatus,
                          @NonNull RedirectAttributes attributes) {

        attributes.addFlashAttribute(INT_FLAGS, model.getAttribute(INT_FLAGS));
        attributes.addFlashAttribute(SINGLE_PLAYER, model.getAttribute(SINGLE_PLAYER));
        attributes.addFlashAttribute(GAME_TOKEN, model.getAttribute(GAME_TOKEN));

        Deck gameDeck = (Deck) model.getAttribute(GAME_DECK);
        assert gameDeck != null;
        if (gameDeck.getUserDeck().isEmpty()) {
            model.addAttribute("winnerValue", 2);
            sessionStatus.setComplete();
            return "winner";
        } else if (gameDeck.getAiDeck().isEmpty()) {
            model.addAttribute("winnerValue", 1);
            sessionStatus.setComplete();
            return "winner";
        } else {
            Deck deck = GameHelper.getHands(gameDeck);
            attributes.addFlashAttribute(GAME_DECK, deck);
            return "redirect:game";
        }
    }

    @GetMapping("/game")
    public String game(@NonNull Model model, @NonNull HttpSession session) {

        session.setAttribute(GAME_DECK, model.getAttribute(GAME_DECK));
        session.setAttribute(INT_FLAGS, model.getAttribute(INT_FLAGS));
        session.setAttribute(SINGLE_PLAYER, model.getAttribute(SINGLE_PLAYER));
        session.setAttribute(GAME_TOKEN, model.getAttribute(GAME_TOKEN));

        Deck gameDeck = (Deck) model.getAttribute(GAME_DECK);
        FlagSet intFlags = (FlagSet) model.getAttribute(INT_FLAGS);
        Integer isSinglePlayer = (Integer) model.getAttribute(SINGLE_PLAYER);
        String gameToken = (String) model.getAttribute(GAME_TOKEN);

        assert gameDeck != null;
        Player player = gameDeck.getSideStack().get(0);
        model.addAttribute("currentUserPlayer",
                GameHelper.getCardFaceView(player));
        model.addAttribute("currentUserCountry",
                CardManager.getShortName(player.getCountry()).name());
        model.addAttribute("currentAiPlayer", new CardFaceView());
        model.addAttribute("currentAiCountry", null);
        model.addAttribute(GAME_DECK, gameDeck);
        model.addAttribute(INT_FLAGS, intFlags);
        model.addAttribute(SINGLE_PLAYER, isSinglePlayer);
        model.addAttribute(GAME_TOKEN, gameToken);

        return "game";
    }

    @Contract(pure = true)
    @PostMapping("/getAiPlayer")
    @ResponseBody
    public @NonNull GamePostResponse getAiPlayer(
            @RequestBody @NonNull GamePostRequest request,
            @SessionAttribute(GAME_DECK) @NonNull Deck gameDeck) {

        CardFaceView aiPlayer = GameHelper.getCardFaceView(gameDeck.getSideStack().get(1));
        String countryShortName = CardManager.getShortName(aiPlayer.getCountry()).name();
        String gameToken = request.getGameToken();
        String statSelected = request.getStatSelected();
        simpMessagingTemplate.convertAndSend("/topic/game-progress/" + gameToken,
                statSelected);

        return new GamePostResponse(aiPlayer, countryShortName);
    }

    @PostMapping("/game")
    public String gamePageSubmit(@RequestParam("statSelected") String statSelected,
                                 @SessionAttribute(GAME_DECK) Deck gameDeck,
                                 @SessionAttribute(INT_FLAGS) @NonNull FlagSet intFlags,
                                 @SessionAttribute(SINGLE_PLAYER) @NonNull Integer isSinglePlayer,
                                 @SessionAttribute(GAME_TOKEN) String gameToken,
                                 RedirectAttributes attributes) {

        GameTurnComparator.Stat statEnum = EnumUtils.getEnumIgnoreCase(
                GameTurnComparator.Stat.class, statSelected);
        int turnWinner = GameTurnComparator.getTurnWinner(gameDeck, statEnum);
        LinkedList<Player> sideStack = gameDeck.getSideStack();
        Collections.sort(sideStack);
        if (turnWinner == 0) {
            gameDeck.getUserDeck().addAll(sideStack);
            gameDeck.getSideStack().clear();
            intFlags.setTurnOwner(turnWinner);
        } else if (turnWinner == 1) {
            gameDeck.getAiDeck().addAll(sideStack);
            gameDeck.getSideStack().clear();
            intFlags.setTurnOwner(turnWinner);
        }
        attributes.addFlashAttribute(GAME_DECK, gameDeck);
        attributes.addFlashAttribute(INT_FLAGS, intFlags);
        attributes.addFlashAttribute(SINGLE_PLAYER, isSinglePlayer);
        attributes.addFlashAttribute(GAME_TOKEN, gameToken);

        return REF_REDIRECT;
    }

    @PostMapping("/connect")
    public String connectToSession(@RequestParam("token") String token,
                                   @NonNull RedirectAttributes attributes)
            throws SQLException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String response = GameHelper.getSession(token);
        GameHelper.updateConnectedSession(token);
        Deck deck = GameHelper.flipDeck(mapper.readValue(response, Deck.class));
        FlagSet flagSet = new FlagSet();
        flagSet.setTurnOwner(1);
        attributes.addFlashAttribute(GAME_DECK, deck);
        attributes.addFlashAttribute(INT_FLAGS, flagSet);
        attributes.addFlashAttribute(SINGLE_PLAYER, 0);
        attributes.addFlashAttribute(GAME_TOKEN, token);
        simpMessagingTemplate.convertAndSend("/topic/game-join/" + token,
                1);

        return REF_REDIRECT;
    }

    @Contract(pure = true)
    @PostMapping("/isTokenValid")
    @ResponseBody
    public static @NonNull String isTokenValid(
            @RequestBody @NonNull Map<String, String> requestBody)
            throws SQLException {

        String token = requestBody.get("token");
        boolean isTokenInvalid = GameHelper.isSessionFree(token);
        if (isTokenInvalid) {
            return token;
        } else {
            return "INVALID";
        }
    }
}
