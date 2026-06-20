# Game Loop Phases

This document describes the runtime frame pipeline owned by `GameScreen` and the phase classes in `com.dawn.game`.

## Active Frame Order

`GameScreen.render(delta)` runs these high-level steps when not paused:

1. UI mode transitions and input routing (`UiModePhase`)
2. Zoom update/apply (`ZoomController`)
3. World update (`GameScreen.update`)
   1. `world.clock().advance(...)`
   2. `PlayerAndInteractionPhase.tickPlayer(...)`
   3. `CameraTargetPhase.tick(...)`
   4. `PlayerAndInteractionPhase.tickInteraction(...)`
   5. `SimulationLightingPhase.tick(...)`
4. `ScreenRenderer.render(...)`

The call order above is intentional and should be treated as a behavioral contract.

## Paused Frame Behavior

When paused:

- `GameScreen.update(...)` is skipped
- `PauseOverlay` acts
- camera is still synced through `CameraTargetPhase.syncCamera(...)`
- target/mouse unproject are not refreshed

This keeps pause visuals stable while preventing simulation/gameplay progression.

## Phase Responsibilities

- `UiModePhase`: pause/inventory toggles, input processor routing, hotbar scroll/update.
- `PlayerAndInteractionPhase`: movement, vitals/status ticks, interaction systems, drops, animation intent.
- `CameraTargetPhase`: camera clamp/follow, mouse unproject, target resolution.
- `SimulationLightingPhase`: simulation region activation, game loop ticks, held light sync, lighting rebuild flush.

## Refactor Guardrails

- Keep `CameraTargetPhase.tick(...)` after `tickPlayer(...)` and before `tickInteraction(...)`.
- Keep `SimulationLightingPhase.tick(...)` after interaction updates and before render.
- Do not run simulation/lighting on paused frames.
- If extracting further, preserve behavior first, then simplify.

## Smoke Checklist (Quick)

- movement and run feel unchanged
- hover target aligns with cursor at multiple zoom levels
- mining/placing/eating still resolve on intended cell
- lantern light follows player and clears when unequipped
- pause freezes progression and resume restores normal behavior
