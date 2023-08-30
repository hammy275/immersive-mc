## Notes

- This is all VR only!

## Info

- Holds whether this instance is queued for removal
- Holds the last hand this info was seen in. registerAndTickAll uses this to communicate to the other functions about where to render and to keep track of items.
- Holds the itemstack

## Main instance

- Stores list of all infos for this immersive
- Protected
  - render(info, poseStack, itemStack)
    - Called by renderAll to render this
  - tick(info, itemStack)
    - Called by registerAndTickAll to tick if the item is valid and info isn't marked for removal
  - itemMatch(itemStack)
    - Called by registerAndTickAll for registering and whether to mark for removal
- Public
  - registerAndTickAll(mainStack, offStack)
    - Handles creating new info instances and ticking all of them.
  - renderAll(poseStack, leftStack, rightStack)
    - Handles rendering all info instances