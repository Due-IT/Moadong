import styled from 'styled-components';

export const HeaderStyles = styled.header`
  position: fixed;
  display: flex;
  flex-direction: row;
  left: 0;
  top: 0;
  width: 100%;
  height: 60px;
  z-index: 9999;
`;

export const LogoButtonStyles = styled.button`
  @font-face {
    font-family: 'CustomFont';
    src: url('../../../assets/fonts/Dream Beige.otf') format('opentype');
    font-weight: normal;
    font-style: normal;
  }
  font-family: Dream Beige;
  flex: none;
  flex-grow: 0;
  border: 0;
  font-style: normal;
  font-weight: 400;
  font-size: 36px;
  height: 60px;
  line-height: 60px;
  letter-spacing: -0.03em;
  background-color: transparent;
  color: #000000;
  margin-left: 140px;
  cursor: pointer;
`;

export const introduceButtonStyles = styled.button`
  flex: none;
  flex-grow: 0;
  width: 81px;
  height: 60px;
  font-weight: 500;
  font-size: 18px;
  line-height: 60px;
  text-align: right;
  letter-spacing: -0.02em;
  color: #262626;
  margin-left: 70px;
  border: none;
  background: transparent;
  cursor: pointer;
`;
